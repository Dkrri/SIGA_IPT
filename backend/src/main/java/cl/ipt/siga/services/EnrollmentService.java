package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.enrollment_dto.AvailableSubjectResponse;
import cl.ipt.siga.dtos.enrollment_dto.EnrollmentRequest;
import cl.ipt.siga.dtos.enrollment_dto.EnrollmentResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.mappers.EnrollmentMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.MatriculationRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business logic for Epic 6: academic enrollment.
 *
 * Rules applied here:
 *  I1  Student REGULAR and matriculated in the period.
 *  I2  Subjects from their plan and offered in the period.
 *  I3  OPEN period to enroll and cancel.
 *  I4  Do not re-enroll an approved subject (a failed one can be re-enrolled).
 *  I5  All prerequisites approved.
 *  I6  Do not enroll the same subject twice in the period.
 *  I7  Minimum 3 / maximum 6 (exception when fewer than 3 pending subjects remain).
 *  I8  Do not enroll a higher semester while lower pending subjects are available.
 *  I9  The section must have available seats.
 *  I10 No schedule conflict between the selected sections.
 *  I11 Student, subject, section, period and date are stored.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final int MIN_SUBJECTS = 3;
    private static final int MAX_SUBJECTS = 6;

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final MatriculationRepository matriculationRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final EnrollmentMapper enrollmentMapper;

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    @Transactional
    public List<EnrollmentResponse> enroll(EnrollmentRequest request) {
        StudentEntity student = getStudentOrThrow(request.getStudentId());
        List<SectionEntity> sections = loadSections(request.getSectionIds());
        AcademicPeriodEntity period = resolveSinglePeriod(sections);

        validatePeriodIsOpen(period);
        validateStudentCanEnroll(student, period);

        List<SubjectEntity> subjects = sections.stream()
                .map(SectionEntity::getSubject)
                .toList();

        validateNoDuplicateSubjects(subjects);
        for (SubjectEntity subject : subjects) {
            validateSubjectBelongsToPlan(student, subject);
            validateSubjectIsNotAlreadyApproved(student, subject);
            validatePrerequisitesApproved(student, subject);
            validateNotAlreadyEnrolled(student, subject, period);
        }

        validateSectionsHaveCapacity(sections);
        validateNoScheduleConflict(sections);
        validateSubjectCount(student, period, subjects);
        validateSemesterOrder(student, period, subjects);

        List<EnrollmentEntity> enrollments = sections.stream()
                .map(section -> enrollmentMapper.toEntity(
                        student, section.getSubject(), section, period))
                .toList();

        return enrollmentRepository.saveAll(enrollments).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public void cancel(Long enrollmentId, Long studentId) {
        EnrollmentEntity enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new BusinessException("You can only cancel your own enrollments");
        }

        validatePeriodIsOpen(enrollment.getAcademicPeriod());

        enrollmentRepository.delete(enrollment);
    }

    // ------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudentAndPeriod(Long studentId, Long academicPeriodId) {
        StudentEntity student = getStudentOrThrow(studentId);
        AcademicPeriodEntity period = getPeriodOrThrow(academicPeriodId);

        return enrollmentRepository.findByStudentAndAcademicPeriod(student, period).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    /** Available offer for the student: plan subjects, offered and not approved. */
    @Transactional(readOnly = true)
    public List<AvailableSubjectResponse> findAvailableSubjects(Long studentId, Long academicPeriodId) {
        StudentEntity student = getStudentOrThrow(studentId);
        AcademicPeriodEntity period = getPeriodOrThrow(academicPeriodId);

        List<AvailableSubjectResponse> available = new ArrayList<>();

        List<SubjectEntity> planSubjects =
                subjectRepository.findByStudyPlanOrderBySemesterAscCodeAsc(student.getStudyPlan());

        for (SubjectEntity subject : planSubjects) {
            if (isApproved(student, subject)) {
                continue;
            }
            if (enrollmentRepository.existsByStudentAndSubjectAndAcademicPeriod(
                    student, subject, period)) {
                continue;
            }
            sectionRepository.findBySubjectAndAcademicPeriod(subject, period)
                    .ifPresent(section -> available.add(
                            enrollmentMapper.toAvailableSubject(
                                    section, enrollmentRepository.countBySection(section))));
        }

        return available;
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private StudentEntity getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    }

    private AcademicPeriodEntity getPeriodOrThrow(Long academicPeriodId) {
        return academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found: " + academicPeriodId));
    }

    private List<SectionEntity> loadSections(List<Long> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) {
            throw new BusinessException("You must select at least one section");
        }
        List<SectionEntity> sections = new ArrayList<>();
        for (Long sectionId : sectionIds) {
            sections.add(sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId)));
        }
        return sections;
    }

    private AcademicPeriodEntity resolveSinglePeriod(List<SectionEntity> sections) {
        AcademicPeriodEntity period = sections.get(0).getAcademicPeriod();
        for (SectionEntity section : sections) {
            if (!section.getAcademicPeriod().getId().equals(period.getId())) {
                throw new BusinessException("All sections must belong to the same period");
            }
        }
        return period;
    }

    /** I3 */
    private void validatePeriodIsOpen(AcademicPeriodEntity period) {
        if (period.getStatus() != AcademicPeriodStatus.OPEN) {
            throw new BusinessException(
                    "Academic period " + period.getCode() + " is not OPEN");
        }
    }

    /** I1 */
    private void validateStudentCanEnroll(StudentEntity student, AcademicPeriodEntity period) {
        if (student.getStatus() != StudentStatus.REGULAR) {
            throw new BusinessException("The student is not in REGULAR status");
        }
        if (!matriculationRepository.existsByStudentAndAcademicPeriod(student, period)) {
            throw new BusinessException(
                    "The student is not matriculated in period " + period.getCode());
        }
    }

    /** I6 (within the same request) */
    private void validateNoDuplicateSubjects(List<SubjectEntity> subjects) {
        Set<Long> ids = new HashSet<>();
        for (SubjectEntity subject : subjects) {
            if (!ids.add(subject.getId())) {
                throw new BusinessException("You cannot enroll in the same subject twice");
            }
        }
    }

    /** I2 */
    private void validateSubjectBelongsToPlan(StudentEntity student, SubjectEntity subject) {
        if (!subject.getStudyPlan().getId().equals(student.getStudyPlan().getId())) {
            throw new BusinessException("Subject " + subject.getCode()
                    + " does not belong to the student's study plan");
        }
    }

    /** I4 */
    private void validateSubjectIsNotAlreadyApproved(StudentEntity student, SubjectEntity subject) {
        if (isApproved(student, subject)) {
            throw new BusinessException("Subject " + subject.getCode() + " is already approved");
        }
    }

    /** I5 */
    private void validatePrerequisitesApproved(StudentEntity student, SubjectEntity subject) {
        for (SubjectEntity prerequisite : subject.getPrerequisites()) {
            if (!isApproved(student, prerequisite)) {
                throw new BusinessException("Prerequisite not approved: "
                        + prerequisite.getCode() + " to enroll in " + subject.getCode());
            }
        }
    }

    /** I6 (against the database) */
    private void validateNotAlreadyEnrolled(StudentEntity student,
                                            SubjectEntity subject,
                                            AcademicPeriodEntity period) {
        if (enrollmentRepository.existsByStudentAndSubjectAndAcademicPeriod(
                student, subject, period)) {
            throw new BusinessException(
                    "Subject " + subject.getCode() + " is already enrolled in the period");
        }
    }

    /** I9 */
    private void validateSectionsHaveCapacity(List<SectionEntity> sections) {
        for (SectionEntity section : sections) {
            long enrolled = enrollmentRepository.countBySection(section);
            if (enrolled >= section.getCapacity()) {
                throw new BusinessException("The section for subject "
                        + section.getSubject().getCode() + " has no available seats");
            }
        }
    }

    /** I10 */
    private void validateNoScheduleConflict(List<SectionEntity> sections) {
        Set<String> usedBlocks = new HashSet<>();
        for (SectionEntity section : sections) {
            for (ScheduleBlockEntity block : section.getScheduleBlocks()) {
                String key = block.getWeekday() + "-" + block.getModule();
                if (!usedBlocks.add(key)) {
                    throw new BusinessException("Schedule conflict in block " + key);
                }
            }
        }
    }

    /** I7 */
    private void validateSubjectCount(StudentEntity student,
                                      AcademicPeriodEntity period,
                                      List<SubjectEntity> requestedSubjects) {
        long existing = enrollmentRepository.countByStudentAndAcademicPeriod(student, period);
        long total = existing + requestedSubjects.size();

        if (total > MAX_SUBJECTS) {
            throw new BusinessException(
                    "You cannot have more than " + MAX_SUBJECTS + " subjects enrolled in the period");
        }

        long pendingAvailable = countPendingAvailable(student, period);
        int minimum = pendingAvailable < MIN_SUBJECTS ? (int) pendingAvailable : MIN_SUBJECTS;

        if (total < minimum) {
            throw new BusinessException(
                    "You must enroll in at least " + minimum + " subjects in the period");
        }
    }

    /** I8 */
    private void validateSemesterOrder(StudentEntity student,
                                       AcademicPeriodEntity period,
                                       List<SubjectEntity> requestedSubjects) {
        List<SubjectEntity> planSubjects =
                subjectRepository.findByStudyPlanOrderBySemesterAscCodeAsc(student.getStudyPlan());

        Set<Long> requestedIds = requestedSubjects.stream()
                .map(SubjectEntity::getId)
                .collect(Collectors.toSet());

        for (SubjectEntity requested : requestedSubjects) {
            boolean hasLowerPending = planSubjects.stream()
                    .filter(subject -> subject.getSemester() < requested.getSemester())
                    .filter(subject -> !requestedIds.contains(subject.getId()))
                    .anyMatch(subject -> isPendingAvailable(student, subject, period));

            if (hasLowerPending) {
                throw new BusinessException("You cannot enroll in " + requested.getCode()
                        + ": you have pending subjects from lower semesters");
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private boolean isApproved(StudentEntity student, SubjectEntity subject) {
        return academicRecordRepository.existsByStudentAndSubjectAndResult(
                student, subject, EnrollmentResult.APPROVED);
    }

    /** Pending = not approved, not yet enrolled and offered in the period. */
    private boolean isPendingAvailable(StudentEntity student,
                                       SubjectEntity subject,
                                       AcademicPeriodEntity period) {
        if (isApproved(student, subject)) {
            return false;
        }
        if (enrollmentRepository.existsByStudentAndSubjectAndAcademicPeriod(
                student, subject, period)) {
            return false;
        }
        return sectionRepository.existsBySubjectAndAcademicPeriod(subject, period);
    }

    private long countPendingAvailable(StudentEntity student, AcademicPeriodEntity period) {
        return subjectRepository.findByStudyPlanOrderBySemesterAscCodeAsc(student.getStudyPlan())
                .stream()
                .filter(subject -> isPendingAvailable(student, subject, period))
                .count();
    }
}
