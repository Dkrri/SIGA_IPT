package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.subject_dto.SubjectRequest;
import cl.ipt.siga.dtos.subject_dto.SubjectResponse;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.mappers.SubjectMapper;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic for Epic 3: subjects and prerequisites.
 *
 * Rules applied here:
 *  - Code unique within the study plan.
 *  - Semester between 1 and 4.
 *  - SCT greater than 0 and less than or equal to 7.
 *  - TEL hours non-negative and sum less than 8.
 *  - Between 0 and 3 prerequisites, from the same plan and previous semesters.
 *  - A subject with history/enrollments/sections is not physically deleted.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class SubjectService {

    private static final int MAX_PREREQUISITES = 3;

    private final SubjectRepository subjectRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final SectionRepository sectionRepository;
    private final SubjectMapper subjectMapper;

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        StudyPlanEntity plan = getPlanOrThrow(request.getStudyPlanId());

        validateFields(request);
        validateCodeIsUniqueInPlan(plan, request.getCode());

        Set<SubjectEntity> prerequisites =
                resolvePrerequisites(request.getPrerequisiteIds(), plan, null);
        validatePrerequisites(prerequisites, request.getSemester());

        SubjectEntity subject = subjectMapper.toEntity(request, plan, prerequisites);
        return subjectMapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        SubjectEntity subject = getOrThrow(id);

        // A subject cannot move between plans: it would alter its history.
        if (!subject.getStudyPlan().getId().equals(request.getStudyPlanId())) {
            throw new BusinessException("A subject cannot change its study plan");
        }
        StudyPlanEntity plan = subject.getStudyPlan();

        validateFields(request);
        validateCodeIsUniqueInPlanForUpdate(plan, request.getCode(), id);

        Set<SubjectEntity> prerequisites =
                resolvePrerequisites(request.getPrerequisiteIds(), plan, id);
        validatePrerequisites(prerequisites, request.getSemester());

        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setSemester(request.getSemester());
        subject.setTheoryHours(request.getTheoryHours());
        subject.setExerciseHours(request.getExerciseHours());
        subject.setLabHours(request.getLabHours());
        subject.setSct(request.getSct());
        subject.setPrerequisites(prerequisites);

        return subjectMapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        SubjectEntity subject = getOrThrow(id);
        validateCanBeDeleted(subject);
        subjectRepository.delete(subject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll() {
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        return subjectMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findByStudyPlan(Long studyPlanId) {
        StudyPlanEntity plan = getPlanOrThrow(studyPlanId);
        return subjectRepository.findByStudyPlanOrderBySemesterAscCodeAsc(plan).stream()
                .map(subjectMapper::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private StudyPlanEntity getPlanOrThrow(Long studyPlanId) {
        return studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found: " + studyPlanId));
    }

    private SubjectEntity getOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));
    }

    /** Simple range rules and the TEL hours sum. */
    private void validateFields(SubjectRequest request) {
        if (request.getSemester() == null || request.getSemester() < 1 || request.getSemester() > 4) {
            throw new BusinessException("Semester must be between 1 and 4");
        }
        if (request.getSct() == null || request.getSct() <= 0 || request.getSct() > 7) {
            throw new BusinessException("SCT must be greater than 0 and less than or equal to 7");
        }
        validateTelHours(request);
    }

    private void validateTelHours(SubjectRequest request) {
        int theory = request.getTheoryHours() == null ? 0 : request.getTheoryHours();
        int exercise = request.getExerciseHours() == null ? 0 : request.getExerciseHours();
        int lab = request.getLabHours() == null ? 0 : request.getLabHours();

        if (theory < 0 || exercise < 0 || lab < 0) {
            throw new BusinessException("TEL hours cannot be negative");
        }
        if (theory + exercise + lab >= 8) {
            throw new BusinessException("The sum of TEL hours must be less than 8");
        }
    }

    private void validateCodeIsUniqueInPlan(StudyPlanEntity plan, String code) {
        if (subjectRepository.existsByStudyPlanAndCode(plan, code)) {
            throw new BusinessException(
                    "A subject already exists with code " + code + " in plan " + plan.getCode());
        }
    }

    private void validateCodeIsUniqueInPlanForUpdate(StudyPlanEntity plan, String code, Long subjectId) {
        if (subjectRepository.existsByStudyPlanAndCodeAndIdNot(plan, code, subjectId)) {
            throw new BusinessException(
                    "Another subject already exists with code " + code + " in plan " + plan.getCode());
        }
    }

    /** Converts request ids into entities, validating they belong to the same plan. */
    private Set<SubjectEntity> resolvePrerequisites(Set<Long> ids, StudyPlanEntity plan, Long currentSubjectId) {
        Set<SubjectEntity> result = new HashSet<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        if (ids.size() > MAX_PREREQUISITES) {
            throw new BusinessException("A subject can have between 0 and 3 prerequisites");
        }

        for (Long id : ids) {
            if (currentSubjectId != null && currentSubjectId.equals(id)) {
                throw new BusinessException("A subject cannot be a prerequisite of itself");
            }
            SubjectEntity prerequisite = getOrThrow(id);
            if (!prerequisite.getStudyPlan().getId().equals(plan.getId())) {
                throw new BusinessException(
                        "Prerequisite " + prerequisite.getCode() + " does not belong to the same study plan");
            }
            result.add(prerequisite);
        }
        return result;
    }

    /** Prerequisites must be from previous semesters. */
    private void validatePrerequisites(Set<SubjectEntity> prerequisites, Integer semester) {
        for (SubjectEntity prerequisite : prerequisites) {
            if (prerequisite.getSemester() >= semester) {
                throw new BusinessException(
                        "Prerequisite " + prerequisite.getCode()
                                + " must be from a previous semester");
            }
        }
    }

    /** Rule: not deleted if it has history, enrollments or sections. */
    private void validateCanBeDeleted(SubjectEntity subject) {
        if (academicRecordRepository.existsBySubject(subject)) {
            throw new BusinessException(
                    "Cannot delete: the subject is part of the academic history");
        }
        if (enrollmentRepository.existsBySubject(subject)) {
            throw new BusinessException("Cannot delete: the subject has enrollments");
        }
        if (sectionRepository.existsBySubject(subject)) {
            throw new BusinessException("Cannot delete: the subject has associated sections");
        }
    }
}
