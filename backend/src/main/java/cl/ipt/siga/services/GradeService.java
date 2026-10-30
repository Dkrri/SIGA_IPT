package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.grade_dto.GradeItemRequest;
import cl.ipt.siga.dtos.grade_dto.GradeRequest;
import cl.ipt.siga.dtos.grade_dto.GradeResponse;
import cl.ipt.siga.dtos.grade_dto.SectionGradesRequest;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.mappers.GradeMapper;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for Epic 7: final grade registration.
 *
 * Rules applied here:
 *  - Only the responsible teacher of the section registers its grades.
 *  - Only for students enrolled in the section.
 *  - Grade in the 1.0 - 7.0 range, editable while the period is OPEN.
 *  - Grade >= 4.0 -> APPROVED; < 4.0 -> FAILED.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class GradeService {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final GradeMapper gradeMapper;

    /** Registers or updates an enrollment grade. */
    @Transactional
    public GradeResponse registerGrade(Long enrollmentId, GradeRequest request, Long teacherId) {
        EnrollmentEntity enrollment = getEnrollmentOrThrow(enrollmentId);

        validatePeriodIsOpen(enrollment.getAcademicPeriod());
        validateTeacherOwnsSection(enrollment.getSection(), teacherId);
        validateGradeRange(request.getFinalGrade());

        applyGrade(enrollment, request.getFinalGrade());
        return gradeMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    /** Bulk grade upload for a section. */
    @Transactional
    public List<GradeResponse> registerGrades(Long sectionId,
                                              SectionGradesRequest request,
                                              Long teacherId) {
        SectionEntity section = getSectionOrThrow(sectionId);

        validatePeriodIsOpen(section.getAcademicPeriod());
        validateTeacherOwnsSection(section, teacherId);

        List<GradeResponse> responses = new ArrayList<>();

        for (GradeItemRequest item : request.getGrades()) {
            EnrollmentEntity enrollment = getEnrollmentOrThrow(item.getEnrollmentId());

            if (!enrollment.getSection().getId().equals(sectionId)) {
                throw new BusinessException("Enrollment " + item.getEnrollmentId()
                        + " does not belong to section " + sectionId);
            }
            validateGradeRange(item.getFinalGrade());
            applyGrade(enrollment, item.getFinalGrade());

            responses.add(gradeMapper.toResponse(enrollmentRepository.save(enrollment)));
        }

        return responses;
    }

    /** Enrollments and grades of a section. Only its responsible teacher. */
    @Transactional(readOnly = true)
    public List<GradeResponse> findBySection(Long sectionId, Long teacherId) {
        SectionEntity section = getSectionOrThrow(sectionId);
        validateTeacherOwnsSection(section, teacherId);
        return enrollmentRepository.findBySection(section).stream()
                .map(gradeMapper::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private EnrollmentEntity getEnrollmentOrThrow(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));
    }

    private SectionEntity getSectionOrThrow(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
    }

    private void validatePeriodIsOpen(AcademicPeriodEntity period) {
        if (period.getStatus() != AcademicPeriodStatus.OPEN) {
            throw new BusinessException(
                    "Academic period " + period.getCode() + " is not OPEN");
        }
    }

    /** Ownership rule: only the section's responsible teacher. */
    private void validateTeacherOwnsSection(SectionEntity section, Long teacherId) {
        if (teacherId == null || !section.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(
                    "Only the teacher responsible for the section can register grades");
        }
    }

    private void validateGradeRange(Double grade) {
        if (grade == null || grade < 1.0 || grade > 7.0) {
            throw new BusinessException("Grade must be between 1.0 and 7.0");
        }
    }

    private void applyGrade(EnrollmentEntity enrollment, Double grade) {
        enrollment.setFinalGrade(grade);
        enrollment.setResult(grade >= 4.0
                ? EnrollmentResult.APPROVED
                : EnrollmentResult.FAILED);
    }
}
