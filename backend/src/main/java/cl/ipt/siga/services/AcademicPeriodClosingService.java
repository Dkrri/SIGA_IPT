package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.closing_dto.ClosingResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.AcademicRecordEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for Epic 7: academic period closing.
 *
 * Rules applied here:
 *  - Only an OPEN period can be closed.
 *  - Every enrollment must have a final grade.
 *  - Grades and results are consolidated into the academic history.
 *  - Second failure of a subject -> ELIMINATED.
 *  - Every subject of the plan approved -> GRADUATED.
 *  - When done, the period becomes CLOSED.
 *
 * Everything runs in one transaction: consolidation + statuses + closing.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class AcademicPeriodClosingService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public ClosingResponse close(Long academicPeriodId) {
        AcademicPeriodEntity period = getPeriodOrThrow(academicPeriodId);

        // Only an OPEN period can be closed
        if (period.getStatus() != AcademicPeriodStatus.OPEN) {
            throw new BusinessException(
                    "Only an OPEN period can be closed (current status: "
                            + period.getStatus() + ")");
        }

        // Every enrollment must have a final grade
        List<EnrollmentEntity> pending =
                enrollmentRepository.findByAcademicPeriodAndFinalGradeIsNull(period);
        if (!pending.isEmpty()) {
            String detail = pending.stream()
                    .map(enrollment -> enrollment.getSection().getSubject().getCode()
                            + " (section " + enrollment.getSection().getId() + ")")
                    .distinct()
                    .collect(Collectors.joining(", "));
            throw new BusinessException(
                    "Cannot close: there are pending grades in: " + detail);
        }

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByAcademicPeriod(period);
        List<StudentEntity> students = enrollments.stream()
                .map(EnrollmentEntity::getStudent)
                .distinct()
                .toList();

        // Consolidate grades and results into the academic history
        for (EnrollmentEntity enrollment : enrollments) {
            EnrollmentResult result = enrollment.getFinalGrade() >= 4.0
                    ? EnrollmentResult.APPROVED
                    : EnrollmentResult.FAILED;

            enrollment.setResult(result);
            enrollmentRepository.save(enrollment);

            AcademicRecordEntity record = new AcademicRecordEntity();
            record.setStudent(enrollment.getStudent());
            record.setSubject(enrollment.getSubject());
            record.setAcademicPeriod(period);
            record.setFinalGrade(enrollment.getFinalGrade());
            record.setResult(result);
            record.setConsolidatedAt(LocalDateTime.now());
            academicRecordRepository.save(record);
        }

        // Update the academic status of affected students
        int graduated = 0;
        int eliminated = 0;

        for (StudentEntity student : students) {
            StudentStatus newStatus = calculateStatus(student);

            if (newStatus == StudentStatus.ELIMINATED) {
                eliminated++;
            }
            if (newStatus == StudentStatus.GRADUATED) {
                graduated++;
            }
            if (newStatus != null) {
                student.setStatus(newStatus);
                studentRepository.save(student);
            }
        }

        // Close the period
        period.setStatus(AcademicPeriodStatus.CLOSED);
        academicPeriodRepository.save(period);

        return new ClosingResponse(
                period.getId(),
                period.getCode(),
                period.getStatus(),
                enrollments.size(),
                graduated,
                eliminated);
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private AcademicPeriodEntity getPeriodOrThrow(Long id) {
        return academicPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found: " + id));
    }

    /**
     * Returns the student's new status, or null when it should not change.
     * Priority: ELIMINATED (two failures of the same subject) over GRADUATED.
     */
    private StudentStatus calculateStatus(StudentEntity student) {
        List<AcademicRecordEntity> records = academicRecordRepository.findByStudent(student);

        // Two failures of the SAME subject
        Map<Long, Long> failedBySubject = records.stream()
                .filter(record -> record.getResult() == EnrollmentResult.FAILED)
                .collect(Collectors.groupingBy(
                        record -> record.getSubject().getId(),
                        Collectors.counting()));

        boolean failedTwice = failedBySubject.values().stream()
                .anyMatch(count -> count >= 2);

        if (failedTwice) {
            return StudentStatus.ELIMINATED;
        }

        // Every subject of the plan approved
        List<SubjectEntity> planSubjects =
                subjectRepository.findByStudyPlanOrderBySemesterAscCodeAsc(student.getStudyPlan());

        boolean allApproved = !planSubjects.isEmpty() && planSubjects.stream().allMatch(subject ->
                records.stream().anyMatch(record ->
                        record.getSubject().getId().equals(subject.getId())
                                && record.getResult() == EnrollmentResult.APPROVED));

        if (allApproved) {
            return StudentStatus.GRADUATED;
        }

        return null;
    }
}
