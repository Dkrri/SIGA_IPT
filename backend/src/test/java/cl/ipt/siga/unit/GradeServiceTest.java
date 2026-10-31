package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.grade_dto.GradeItemRequest;
import cl.ipt.siga.dtos.grade_dto.GradeRequest;
import cl.ipt.siga.dtos.grade_dto.GradeResponse;
import cl.ipt.siga.dtos.grade_dto.SectionGradesRequest;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.GradeMapper;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.services.GradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private GradeMapper mapper;
    @InjectMocks
    private GradeService service;

    private final TeacherEntity teacher = TestData.teacher(10L, "teacher@test.cl");
    private final AcademicPeriodEntity period = TestData.period(20L, AcademicPeriodStatus.OPEN);
    private final SubjectEntity subject = TestData.subject(30L, TestData.plan(1L, TestData.career(1L)), 1, 2);
    private final SectionEntity section = TestData.section(40L, subject, period, teacher, 30);
    private final EnrollmentEntity enrollment = TestData.enrollment(50L, TestData.student(60L, "s@test.cl", TestData.career(1L), TestData.plan(1L, TestData.career(1L))), section, null, EnrollmentResult.PENDING);

    private GradeRequest grade(Double value) {
        GradeRequest request = new GradeRequest();
        request.setFinalGrade(value);
        return request;
    }

    @Test
    void registerApprovedGrade() {
        GradeResponse response = new GradeResponse();
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(mapper.toResponse(enrollment)).thenReturn(response);

        assertSame(response, service.registerGrade(50L, grade(5.5), 10L));
        assertEquals(EnrollmentResult.APPROVED, enrollment.getResult());
    }

    @Test
    void registerFailedGrade() {
        GradeResponse response = new GradeResponse();
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(mapper.toResponse(enrollment)).thenReturn(response);

        assertSame(response, service.registerGrade(50L, grade(3.9), 10L));
        assertEquals(EnrollmentResult.FAILED, enrollment.getResult());
    }

    @Test
    void rejectGradeWhenEnrollmentMissing() {
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.registerGrade(50L, grade(5.0), 10L));
    }

    @Test
    void rejectGradeWhenPeriodClosed() {
        period.setStatus(AcademicPeriodStatus.CLOSED);
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        assertThrows(BusinessException.class,
                () -> service.registerGrade(50L, grade(5.0), 10L));
    }

    @Test
    void rejectGradeFromOtherTeacher() {
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        assertThrows(BusinessException.class,
                () -> service.registerGrade(50L, grade(5.0), 999L));
    }

    @Test
    void rejectGradeWithNullTeacher() {
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        assertThrows(BusinessException.class,
                () -> service.registerGrade(50L, grade(5.0), null));
    }

    @Test
    void rejectGradeOutOfRange() {
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        assertThrows(BusinessException.class,
                () -> service.registerGrade(50L, grade(0.9), 10L));
        assertThrows(BusinessException.class,
                () -> service.registerGrade(50L, grade(7.1), 10L));
        assertThrows(BusinessException.class,
                () -> service.registerGrade(50L, grade(null), 10L));
    }

    @Test
    void registerBulkGrades() {
        EnrollmentEntity second = TestData.enrollment(51L,
                TestData.student(61L, "s2@test.cl", TestData.career(1L), TestData.plan(1L, TestData.career(1L))),
                section, null, EnrollmentResult.PENDING);
        GradeResponse firstResponse = new GradeResponse();
        GradeResponse secondResponse = new GradeResponse();

        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(50L, 6.0), item(51L, 4.0)));

        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.findById(51L)).thenReturn(Optional.of(second));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(enrollmentRepository.save(second)).thenReturn(second);
        when(mapper.toResponse(enrollment)).thenReturn(firstResponse);
        when(mapper.toResponse(second)).thenReturn(secondResponse);

        assertEquals(List.of(firstResponse, secondResponse),
                service.registerGrades(40L, request, 10L));
    }

    @Test
    void rejectBulkWhenSectionMissing() {
        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(50L, 6.0)));
        when(sectionRepository.findById(40L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.registerGrades(40L, request, 10L));
    }

    @Test
    void rejectBulkWhenPeriodClosed() {
        period.setStatus(AcademicPeriodStatus.CLOSED);
        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(50L, 6.0)));
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));

        assertThrows(BusinessException.class,
                () -> service.registerGrades(40L, request, 10L));
    }

    @Test
    void rejectBulkWhenTeacherIsNotOwner() {
        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(50L, 6.0)));
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));

        assertThrows(BusinessException.class,
                () -> service.registerGrades(40L, request, 99L));
    }

    @Test
    void rejectBulkWhenEnrollmentMissing() {
        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(50L, 6.0)));
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.registerGrades(40L, request, 10L));
    }

    @Test
    void rejectBulkWhenEnrollmentBelongsToAnotherSection() {
        SectionEntity other = TestData.section(41L, subject, period, teacher, 30);
        EnrollmentEntity foreign = TestData.enrollment(52L, enrollment.getStudent(), other, null, EnrollmentResult.PENDING);
        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(52L, 6.0)));
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findById(52L)).thenReturn(Optional.of(foreign));

        assertThrows(BusinessException.class,
                () -> service.registerGrades(40L, request, 10L));
    }

    @Test
    void rejectBulkWithInvalidGrade() {
        SectionGradesRequest request = new SectionGradesRequest();
        request.setGrades(List.of(item(50L, 8.0)));
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(enrollment));

        assertThrows(BusinessException.class,
                () -> service.registerGrades(40L, request, 10L));
    }

    @Test
    void findBySectionSuccessfully() {
        GradeResponse response = new GradeResponse();
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findBySection(section)).thenReturn(List.of(enrollment));
        when(mapper.toResponse(enrollment)).thenReturn(response);

        assertEquals(List.of(response), service.findBySection(40L, 10L));
    }

    @Test
    void findBySectionThrowsWhenMissing() {
        when(sectionRepository.findById(40L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findBySection(40L, 10L));
    }

    @Test
    void findBySectionRejectsForeignTeacher() {
        when(sectionRepository.findById(40L)).thenReturn(Optional.of(section));
        assertThrows(BusinessException.class, () -> service.findBySection(40L, 99L));
    }

    private GradeItemRequest item(Long enrollmentId, double grade) {
        GradeItemRequest item = new GradeItemRequest();
        item.setEnrollmentId(enrollmentId);
        item.setFinalGrade(grade);
        return item;
    }
}
