package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.student_dto.StudentRequest;
import cl.ipt.siga.dtos.student_dto.StudentResponse;
import cl.ipt.siga.dtos.student_dto.StudentUpdateRequest;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.enums.StudyPlanStatus;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.StudentMapper;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.services.KeycloakAdminService;
import cl.ipt.siga.services.StudentService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CareerRepository careerRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private AcademicRecordRepository academicRecordRepository;
    @Mock
    private StudentMapper mapper;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @InjectMocks
    private StudentService service;

    private StudentRequest request() {
        StudentRequest request = new StudentRequest();
        request.setRun("11111111-1");
        request.setFirstName("Name");
        request.setLastName("Last");
        request.setEmail("s@test.cl");
        request.setCareerId(1L);
        request.setTemporaryPassword("Temp12345");
        return request;
    }

    private StudentUpdateRequest updateRequest(String email, Long careerId) {
        StudentUpdateRequest request = new StudentUpdateRequest();
        request.setFirstName("Name");
        request.setLastName("Last");
        request.setEmail(email);
        request.setCareerId(careerId);
        return request;
    }

    @Test
    void createSuccessfully() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, plan);
        StudentResponse response = new StudentResponse();

        when(studentRepository.existsByRun("11111111-1")).thenReturn(false);
        when(studentRepository.existsByEmail("s@test.cl")).thenReturn(false);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan));
        when(mapper.toEntity(any(StudentRequest.class), eq(career), eq(plan))).thenReturn(student);
        when(studentRepository.save(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(response);

        assertSame(response, service.create(request()));
        assertEquals(StudentStatus.REGULAR, student.getStatus());
        verify(keycloakAdminService).createUser("s@test.cl", "Student", "1", "Temp12345", "STUDENT");
    }

    @Test
    void rejectDuplicateRun() {
        when(studentRepository.existsByRun("11111111-1")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void rejectDuplicateEmail() {
        when(studentRepository.existsByRun("11111111-1")).thenReturn(false);
        when(studentRepository.existsByEmail("s@test.cl")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void rejectCreateWhenCareerMissing() {
        when(studentRepository.existsByRun("11111111-1")).thenReturn(false);
        when(studentRepository.existsByEmail("s@test.cl")).thenReturn(false);
        when(careerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
    }

    @Test
    void rejectCreateWhenCareerInactive() {
        CareerEntity career = TestData.career(1L);
        career.setStatus(CareerStatus.INACTIVE);
        when(studentRepository.existsByRun("11111111-1")).thenReturn(false);
        when(studentRepository.existsByEmail("s@test.cl")).thenReturn(false);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));

        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void rejectCreateWithoutCurrentPlan() {
        CareerEntity career = TestData.career(1L);
        when(studentRepository.existsByRun("11111111-1")).thenReturn(false);
        when(studentRepository.existsByEmail("s@test.cl")).thenReturn(false);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void updateWithoutChangingCareerOrEmail() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, plan);
        StudentResponse response = new StudentResponse();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(response);

        assertSame(response, service.update(1L, updateRequest("s@test.cl", 1L)));
    }

    @Test
    void updateWithNewUniqueEmail() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        StudentResponse response = new StudentResponse();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("new@test.cl")).thenReturn(false);
        when(studentRepository.save(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(response);

        assertSame(response, service.update(1L, updateRequest("new@test.cl", 1L)));
        assertEquals("new@test.cl", student.getEmail());
    }

    @Test
    void rejectUpdateWithDuplicateEmail() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("new@test.cl")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, updateRequest("new@test.cl", 1L)));
    }

    @Test
    void updateChangesCareerWithoutHistory() {
        CareerEntity first = TestData.career(1L);
        CareerEntity second = TestData.career(2L);
        StudyPlanEntity secondPlan = TestData.plan(2L, second);
        StudentEntity student = TestData.student(1L, "s@test.cl", first, TestData.plan(1L, first));
        StudentResponse response = new StudentResponse();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudent(student)).thenReturn(false);
        when(academicRecordRepository.existsByStudent(student)).thenReturn(false);
        when(careerRepository.findById(2L)).thenReturn(Optional.of(second));
        when(studyPlanRepository.findByCareerAndStatus(second, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.of(secondPlan));
        when(studentRepository.save(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(response);

        assertSame(response, service.update(1L, updateRequest("s@test.cl", 2L)));
        assertEquals(second, student.getCareer());
        assertEquals(secondPlan, student.getStudyPlan());
    }

    @Test
    void rejectCareerChangeWithHistory() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudent(student)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, updateRequest("s@test.cl", 2L)));
    }

    @Test
    void changeStatusToAllowedValue() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        StudentResponse response = new StudentResponse();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(response);

        assertSame(response, service.changeStatus(1L, StudentStatus.POSTPONEMENT));
        assertEquals(StudentStatus.POSTPONEMENT, student.getStatus());
    }

    @Test
    void rejectManualStatusNotAllowed() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(BusinessException.class, () -> service.changeStatus(1L, StudentStatus.GRADUATED));
    }

    @Test
    void deleteSuccessfullyWithoutHistory() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudent(student)).thenReturn(false);
        when(academicRecordRepository.existsByStudent(student)).thenReturn(false);

        service.delete(1L);
        verify(studentRepository).delete(student);
    }

    @Test
    void rejectDeleteWithHistory() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudent(student)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }

    @Test
    void readOperations() {
        CareerEntity career = TestData.career(1L);
        StudentEntity student = TestData.student(1L, "s@test.cl", career, TestData.plan(1L, career));
        StudentResponse response = new StudentResponse();
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findByRun("11111111-1")).thenReturn(student);
        when(studentRepository.findByEmail("s@test.cl")).thenReturn(Optional.of(student));
        when(mapper.toResponse(student)).thenReturn(response);

        assertEquals(List.of(response), service.findAll());
        assertSame(response, service.findById(1L));
        assertSame(response, service.findByRun("11111111-1"));
        assertSame(response, service.findByEmail("s@test.cl"));
    }

    @Test
    void readOperationsThrowWhenMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        when(studentRepository.findByRun("missing")).thenReturn(null);
        when(studentRepository.findByEmail("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByRun("missing"));
        assertThrows(ResourceNotFoundException.class, () -> service.findByEmail("missing"));
    }
}
