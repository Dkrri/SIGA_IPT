package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.section_dto.ScheduleBlockRequest;
import cl.ipt.siga.dtos.section_dto.SectionRequest;
import cl.ipt.siga.dtos.section_dto.SectionResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.enums.Weekday;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.SectionMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import cl.ipt.siga.repositories.TeacherRepository;
import cl.ipt.siga.services.SectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private SectionMapper mapper;
    @InjectMocks
    private SectionService service;

    private final SubjectEntity subject = TestData.subject(1L, TestData.plan(1L, TestData.career(1L)), 1, 4);
    private final AcademicPeriodEntity period = TestData.period(2L, AcademicPeriodStatus.OPEN);
    private final TeacherEntity teacher = TestData.teacher(3L, "t@test.cl");
    private final SectionEntity section = TestData.section(4L, subject, period, teacher, 30);

    private SectionRequest request(int blocks) {
        SectionRequest request = new SectionRequest();
        request.setSubjectId(1L);
        request.setAcademicPeriodId(2L);
        request.setTeacherId(3L);
        request.setCapacity(30);
        List<ScheduleBlockRequest> list = new ArrayList<>();
        for (int i = 0; i < blocks; i++) {
            ScheduleBlockRequest block = new ScheduleBlockRequest();
            block.setWeekday(Weekday.MONDAY);
            block.setModule(i + 1);
            list.add(block);
        }
        request.setScheduleBlocks(list);
        return request;
    }

    private void stubCreateDependencies(SectionRequest request) {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(false);
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period)).thenReturn(List.of());
        when(mapper.toEntity(request, subject, period, teacher)).thenReturn(section);
        when(sectionRepository.save(section)).thenReturn(section);
    }

    @Test
    void createSuccessfully() {
        SectionRequest request = request(2);
        SectionResponse response = new SectionResponse();
        stubCreateDependencies(request);
        when(mapper.toResponse(section)).thenReturn(response);
        when(enrollmentRepository.countBySection(section)).thenReturn(0L);

        assertSame(response, service.create(request));
    }

    @Test
    void createThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request(2)));
    }

    @Test
    void createThrowsWhenPeriodMissing() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request(2)));
    }

    @Test
    void createThrowsWhenTeacherMissing() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request(2)));
    }

    @Test
    void rejectInactiveTeacher() {
        teacher.setStatus(TeacherStatus.INACTIVE);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));

        assertThrows(BusinessException.class, () -> service.create(request(2)));
    }

    @Test
    void rejectInvalidCapacity() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));

        SectionRequest zero = request(2);
        zero.setCapacity(0);
        assertThrows(BusinessException.class, () -> service.create(zero));

        SectionRequest high = request(2);
        high.setCapacity(61);
        assertThrows(BusinessException.class, () -> service.create(high));

        SectionRequest nullCapacity = request(2);
        nullCapacity.setCapacity(null);
        assertThrows(BusinessException.class, () -> service.create(nullCapacity));
    }

    @Test
    void rejectDuplicateSectionForSubjectAndPeriod() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request(2)));
    }

    @Test
    void rejectEmptyBlocks() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(false);

        SectionRequest request = request(2);
        request.setScheduleBlocks(List.of());
        assertThrows(BusinessException.class, () -> service.create(request));

        SectionRequest nullBlocks = request(2);
        nullBlocks.setScheduleBlocks(null);
        assertThrows(BusinessException.class, () -> service.create(nullBlocks));
    }

    @Test
    void rejectDuplicateBlocks() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(false);

        SectionRequest request = request(2);
        request.getScheduleBlocks().get(1).setModule(1);
        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void rejectOddTel() {
        subject.setTheoryHours(3);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.create(request(1)));
    }

    @Test
    void rejectWrongBlockCount() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.create(request(1)));
    }

    @Test
    void rejectTeacherScheduleConflict() {
        SectionEntity existing = TestData.section(9L, subject, period, teacher, 30);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)).thenReturn(false);
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period))
                .thenReturn(List.of(existing));

        assertThrows(BusinessException.class, () -> service.create(request(2)));
    }

    @Test
    void updateSuccessfully() {
        SectionResponse response = new SectionResponse();
        SectionRequest request = request(2);
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriodAndIdNot(subject, period, 4L)).thenReturn(false);
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period)).thenReturn(List.of(section));
        when(sectionRepository.save(section)).thenReturn(section);
        when(mapper.toResponse(section)).thenReturn(response);
        when(enrollmentRepository.countBySection(section)).thenReturn(0L);

        assertSame(response, service.update(4L, request));
        assertEquals(30, section.getCapacity());
        assertEquals(2, section.getScheduleBlocks().size());
    }

    @Test
    void updateThrowsWhenSectionMissing() {
        when(sectionRepository.findById(4L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(4L, request(2)));
    }

    @Test
    void rejectUpdateWithAnotherSectionForSameSubject() {
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsBySubjectAndAcademicPeriodAndIdNot(subject, period, 4L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(4L, request(2)));
    }

    @Test
    void deleteSuccessfully() {
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.existsBySection(section)).thenReturn(false);

        service.delete(4L);
        verify(sectionRepository).delete(section);
    }

    @Test
    void rejectDeleteWithEnrollments() {
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.existsBySection(section)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(4L));
    }

    @Test
    void readOperations() {
        SectionResponse response = new SectionResponse();
        when(sectionRepository.findAll()).thenReturn(List.of(section));
        when(sectionRepository.findById(4L)).thenReturn(Optional.of(section));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(sectionRepository.findByAcademicPeriod(period)).thenReturn(List.of(section));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.findByTeacher(teacher)).thenReturn(List.of(section));
        when(mapper.toResponse(section)).thenReturn(response);
        when(enrollmentRepository.countBySection(section)).thenReturn(5L);

        assertEquals(1, service.findAll().size());
        assertSame(response, service.findById(4L));
        assertEquals(1, service.findByAcademicPeriod(2L).size());
        assertEquals(1, service.findByTeacher(3L).size());
    }

    @Test
    void readOperationsThrowWhenMissing() {
        when(sectionRepository.findById(4L)).thenReturn(Optional.empty());
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.empty());
        when(teacherRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(4L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByAcademicPeriod(2L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByTeacher(3L));
    }
}
