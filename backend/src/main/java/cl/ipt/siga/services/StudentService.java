package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.student_dto.StudentRequest;
import cl.ipt.siga.dtos.student_dto.StudentUpdateRequest;
import cl.ipt.siga.dtos.student_dto.StudentResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.enums.StudyPlanStatus;
import cl.ipt.siga.mappers.StudentMapper;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static cl.ipt.siga.enums.StudentStatus.REGULAR;

/**
 * Business logic for Epic 1: students.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;
    private final StudentRepository studentRepository;
    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final KeycloakAdminService keycloakAdminService;

    /** Statuses the ADMIN can assign manually. */
    private static final Set<StudentStatus> MANUALLY_ASSIGNABLE =
            Set.of(StudentStatus.REGULAR,
                    StudentStatus.POSTPONEMENT,
                    StudentStatus.TEMPORARY_WITHDRAWAL);

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    @Transactional
    public StudentResponse create(StudentRequest request) {
        validateRunIsUnique(request.getRun());
        validateEmailIsUnique(request.getEmail());

        CareerEntity career = getCareerOrThrow(request.getCareerId());
        validateCareerIsActive(career);
        StudyPlanEntity studyPlan = getCurrentPlanOrThrow(career);

        StudentEntity student = studentMapper.toEntity(request, career, studyPlan);
        student.setStatus(REGULAR);

        StudentEntity saved = studentRepository.save(student);
        keycloakAdminService.createUser(
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                request.getTemporaryPassword(),
                "STUDENT");
        return studentMapper.toResponse(saved);
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        StudentEntity student = getOrThrow(id);

        // Email must remain unique (only validated when it changes)
        if (!student.getEmail().equals(request.getEmail())) {
            validateEmailIsUnique(request.getEmail());
        }

        // Change career only if there is no history
        boolean careerChanged = !student.getCareer().getId().equals(request.getCareerId());
        if (careerChanged) {
            validateHasNoHistory(student);

            CareerEntity career = getCareerOrThrow(request.getCareerId());
            validateCareerIsActive(career);
            StudyPlanEntity studyPlan = getCurrentPlanOrThrow(career);

            student.setCareer(career);
            student.setStudyPlan(studyPlan);
        }

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());

        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse changeStatus(Long id, StudentStatus status) {
        StudentEntity student = getOrThrow(id);
        validateStatusManuallyAssignable(status);
        student.setStatus(status);
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        StudentEntity student = getOrThrow(id);
        validateHasNoHistory(student);
        studentRepository.delete(student);
    }

    @Transactional
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudentResponse findById(Long id) {
        return studentMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public StudentResponse findByRun(String run) {
        StudentEntity student = studentRepository.findByRun(run);
        if (student == null) {
            throw new ResourceNotFoundException("Student not found: " + run);
        }
        return studentMapper.toResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse findByEmail(String email) {
        StudentEntity student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with email: " + email));
        return studentMapper.toResponse(student);
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private StudentEntity getOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private CareerEntity getCareerOrThrow(Long careerId) {
        return careerRepository.findById(careerId)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found: " + careerId));
    }

    private StudyPlanEntity getCurrentPlanOrThrow(CareerEntity career) {
        return studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("The career has no current study plan"));
    }

    private void validateRunIsUnique(String run) {
        if (studentRepository.existsByRun(run)) {
            throw new BusinessException("A student with that RUN already exists");
        }
    }

    private void validateEmailIsUnique(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new BusinessException("A student with that email already exists");
        }
    }

    private void validateStatusManuallyAssignable(StudentStatus status) {
        if (!MANUALLY_ASSIGNABLE.contains(status)) {
            throw new BusinessException("Status " + status + " cannot be assigned manually");
        }
    }

    private void validateCareerIsActive(CareerEntity career) {
        if (career.getStatus() != CareerStatus.ACTIVE) {
            throw new BusinessException("The career is not active and does not accept students");
        }
    }

    /** Rule: with enrollments or history it cannot be deleted or change career/plan. */
    private void validateHasNoHistory(StudentEntity student) {
        if (enrollmentRepository.existsByStudent(student)
                || academicRecordRepository.existsByStudent(student)) {
            throw new BusinessException(
                    "The student has enrollments or academic history; "
                            + "it cannot be deleted or change career/study plan");
        }
    }
}
