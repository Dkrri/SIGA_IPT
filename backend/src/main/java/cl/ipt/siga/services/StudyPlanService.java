package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.studyplan_dto.StudyPlanRequest;
import cl.ipt.siga.dtos.studyplan_dto.StudyPlanResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.enums.StudyPlanStatus;
import cl.ipt.siga.mappers.StudyPlanMapper;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Epic 2: study plans.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;
    private final StudyPlanMapper studyPlanMapper;

    @Transactional
    public StudyPlanResponse create(StudyPlanRequest request) {
        CareerEntity career = getCareerOrThrow(request.getCareerId());
        validateCodeIsUniqueInCareer(career, request.getCode());

        StudyPlanEntity plan = studyPlanMapper.toEntity(request, career);

        if (request.isCurrent()) {
            deactivateCurrentPlan(career);
            plan.setStatus(StudyPlanStatus.ACTIVE);
        } else {
            plan.setStatus(StudyPlanStatus.INACTIVE);
        }

        return studyPlanMapper.toResponse(studyPlanRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> findByCareer(Long careerId) {
        CareerEntity career = getCareerOrThrow(careerId);
        return studyPlanRepository.findByCareerOrderByYearDesc(career).stream()
                .map(studyPlanMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyPlanResponse findCurrent(Long careerId) {
        CareerEntity career = getCareerOrThrow(careerId);
        return studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE)
                .map(studyPlanMapper::toResponse)
                .orElseThrow(() -> new BusinessException(
                        "Career " + career.getCode() + " has no current study plan"));
    }

    /** Marks this plan as the current one for its career. */
    @Transactional
    public StudyPlanResponse setCurrent(Long id) {
        StudyPlanEntity plan = getOrThrow(id);
        deactivateCurrentPlan(plan.getCareer());
        plan.setStatus(StudyPlanStatus.ACTIVE);
        return studyPlanMapper.toResponse(studyPlanRepository.save(plan));
    }

    @Transactional
    public void delete(Long id) {
        StudyPlanEntity plan = getOrThrow(id);
        validateHasNoStudents(plan);
        studyPlanRepository.delete(plan);
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private CareerEntity getCareerOrThrow(Long careerId) {
        return careerRepository.findById(careerId)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found: " + careerId));
    }

    private StudyPlanEntity getOrThrow(Long id) {
        return studyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found: " + id));
    }

    private void validateCodeIsUniqueInCareer(CareerEntity career, String code) {
        if (studyPlanRepository.existsByCareerAndCode(career, code)) {
            throw new BusinessException(
                    "A study plan already exists with code " + code + " in career " + career.getCode());
        }
    }

    private void validateHasNoStudents(StudyPlanEntity plan) {
        if (studentRepository.existsByStudyPlan(plan)) {
            throw new BusinessException("Cannot delete the study plan: it has associated students");
        }
    }

    /**
     * Leaves at most one current plan per career. Changing the status of a JPA-managed
     * entity inside the transaction persists automatically (dirty checking).
     */
    private void deactivateCurrentPlan(CareerEntity career) {
        studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE)
                .ifPresent(current -> current.setStatus(StudyPlanStatus.INACTIVE));
    }
}
