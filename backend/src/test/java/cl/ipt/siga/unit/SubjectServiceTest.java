package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.subject_dto.SubjectRequest;
import cl.ipt.siga.dtos.subject_dto.SubjectResponse;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.SubjectMapper;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import cl.ipt.siga.services.SubjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private AcademicRecordRepository academicRecordRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SubjectMapper mapper;
    @InjectMocks
    private SubjectService service;

    private SubjectRequest request(Long planId, int semester, int sct, int tel) {
        SubjectRequest request = new SubjectRequest();
        request.setCode("S1");
        request.setName("Subject");
        request.setSemester(semester);
        request.setSct(sct);
        request.setTheoryHours(tel);
        request.setExerciseHours(0);
        request.setLabHours(0);
        request.setStudyPlanId(planId);
        request.setPrerequisiteIds(Set.of());
        return request;
    }

    @Test
    void createSuccessfully() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        SubjectResponse response = new SubjectResponse();

        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.existsByStudyPlanAndCode(plan, "S1")).thenReturn(false);
        when(mapper.toEntity(any(SubjectRequest.class), any(StudyPlanEntity.class), anySet()))
                .thenReturn(subject);
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(mapper.toResponse(subject)).thenReturn(response);

        assertSame(response, service.create(request(1L, 1, 4, 2)));
    }

    @Test
    void createThrowsWhenPlanMissing() {
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request(1L, 1, 4, 2)));
    }

    @Test
    void rejectInvalidSemester() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        assertThrows(BusinessException.class, () -> service.create(request(1L, 0, 4, 2)));
        assertThrows(BusinessException.class, () -> service.create(request(1L, 5, 4, 2)));
    }

    @Test
    void rejectInvalidSct() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        assertThrows(BusinessException.class, () -> service.create(request(1L, 1, 0, 2)));
        assertThrows(BusinessException.class, () -> service.create(request(1L, 1, 8, 2)));
    }

    @Test
    void rejectNegativeOrExcessiveTel() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        SubjectRequest negative = request(1L, 1, 4, 2);
        negative.setTheoryHours(-1);
        assertThrows(BusinessException.class, () -> service.create(negative));

        SubjectRequest excessive = request(1L, 1, 4, 8);
        assertThrows(BusinessException.class, () -> service.create(excessive));
    }

    @Test
    void rejectDuplicateCodeOnCreate() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.existsByStudyPlanAndCode(plan, "S1")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request(1L, 1, 4, 2)));
    }

    @Test
    void rejectMoreThanThreePrerequisites() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.existsByStudyPlanAndCode(plan, "S1")).thenReturn(false);

        SubjectRequest request = request(1L, 2, 4, 2);
        request.setPrerequisiteIds(Set.of(1L, 2L, 3L, 4L));
        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void createWithValidPrerequisite() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity prerequisite = TestData.subject(2L, plan, 1, 2);
        SubjectEntity subject = TestData.subject(1L, plan, 2, 2);
        SubjectResponse response = new SubjectResponse();

        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.existsByStudyPlanAndCode(plan, "S1")).thenReturn(false);
        when(subjectRepository.findById(2L)).thenReturn(Optional.of(prerequisite));
        when(mapper.toEntity(any(SubjectRequest.class), any(StudyPlanEntity.class), anySet()))
                .thenReturn(subject);
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(mapper.toResponse(subject)).thenReturn(response);

        SubjectRequest request = request(1L, 2, 4, 2);
        request.setPrerequisiteIds(Set.of(2L));
        assertSame(response, service.create(request));
    }

    @Test
    void rejectPrerequisiteFromAnotherPlan() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        StudyPlanEntity otherPlan = TestData.plan(2L, TestData.career(2L));
        SubjectEntity prerequisite = TestData.subject(2L, otherPlan, 1, 2);
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.existsByStudyPlanAndCode(plan, "S1")).thenReturn(false);
        when(subjectRepository.findById(2L)).thenReturn(Optional.of(prerequisite));

        SubjectRequest request = request(1L, 2, 4, 2);
        request.setPrerequisiteIds(Set.of(2L));
        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void rejectPrerequisiteFromSameOrLaterSemester() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity prerequisite = TestData.subject(2L, plan, 2, 2);
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.existsByStudyPlanAndCode(plan, "S1")).thenReturn(false);
        when(subjectRepository.findById(2L)).thenReturn(Optional.of(prerequisite));

        SubjectRequest request = request(1L, 2, 4, 2);
        request.setPrerequisiteIds(Set.of(2L));
        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void updateSuccessfully() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        SubjectResponse response = new SubjectResponse();

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByStudyPlanAndCodeAndIdNot(plan, "S1", 1L)).thenReturn(false);
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(mapper.toResponse(subject)).thenReturn(response);

        assertSame(response, service.update(1L, request(1L, 1, 4, 2)));
    }

    @Test
    void rejectUpdateWhenSubjectMissing() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, request(1L, 1, 4, 2)));
    }

    @Test
    void rejectChangingStudyPlanOnUpdate() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        assertThrows(BusinessException.class, () -> service.update(1L, request(2L, 1, 4, 2)));
    }

    @Test
    void rejectDuplicateCodeOnUpdate() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByStudyPlanAndCodeAndIdNot(plan, "S1", 1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, request(1L, 1, 4, 2)));
    }

    @Test
    void rejectSubjectAsItsOwnPrerequisite() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByStudyPlanAndCodeAndIdNot(plan, "S1", 1L)).thenReturn(false);

        SubjectRequest request = request(1L, 2, 4, 2);
        request.setPrerequisiteIds(Set.of(1L));
        assertThrows(BusinessException.class, () -> service.update(1L, request));
    }

    @Test
    void deleteSuccessfully() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicRecordRepository.existsBySubject(subject)).thenReturn(false);
        when(enrollmentRepository.existsBySubject(subject)).thenReturn(false);
        when(sectionRepository.existsBySubject(subject)).thenReturn(false);

        service.delete(1L);
        verify(subjectRepository).delete(subject);
    }

    @Test
    void rejectDeleteWhenSubjectHasDependencies() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicRecordRepository.existsBySubject(subject)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }

    @Test
    void rejectDeleteWhenSubjectHasEnrollments() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicRecordRepository.existsBySubject(subject)).thenReturn(false);
        when(enrollmentRepository.existsBySubject(subject)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }

    @Test
    void rejectDeleteWhenSubjectHasSections() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicRecordRepository.existsBySubject(subject)).thenReturn(false);
        when(enrollmentRepository.existsBySubject(subject)).thenReturn(false);
        when(sectionRepository.existsBySubject(subject)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }

    @Test
    void readOperations() {
        StudyPlanEntity plan = TestData.plan(1L, TestData.career(1L));
        SubjectEntity subject = TestData.subject(1L, plan, 1, 2);
        SubjectResponse response = new SubjectResponse();
        when(subjectRepository.findAll()).thenReturn(List.of(subject));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subjectRepository.findByStudyPlanOrderBySemesterAscCodeAsc(plan))
                .thenReturn(List.of(subject));
        when(mapper.toResponse(subject)).thenReturn(response);

        assertEquals(List.of(response), service.findAll());
        assertSame(response, service.findById(1L));
        assertEquals(List.of(response), service.findByStudyPlan(1L));
    }

    @Test
    void readOperationsThrowWhenMissing() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByStudyPlan(1L));
    }
}
