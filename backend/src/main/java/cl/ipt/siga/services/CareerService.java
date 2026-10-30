package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.career_dto.CareerRequest;
import cl.ipt.siga.dtos.career_dto.CareerResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.mappers.CareerMapper;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Epic 2: careers.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class CareerService {

    private static final int CAREER_SEMESTERS = 4;

    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StudentRepository studentRepository;
    private final CareerMapper careerMapper;

    @Transactional
    public CareerResponse create(CareerRequest request) {
        validateCodeIsUnique(request.getCode());

        CareerEntity career = careerMapper.toEntity(request);
        career.setSemesters(CAREER_SEMESTERS);
        career.setStatus(CareerStatus.ACTIVE);

        return careerMapper.toResponse(careerRepository.save(career));
    }

    @Transactional(readOnly = true)
    public List<CareerResponse> findAll() {
        return careerRepository.findAll().stream()
                .map(careerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CareerResponse findById(Long id) {
        return careerMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public CareerResponse update(Long id, CareerRequest request) {
        CareerEntity career = getOrThrow(id);

        if (!career.getCode().equals(request.getCode())) {
            validateCodeIsUnique(request.getCode());
        }

        career.setCode(request.getCode());
        career.setName(request.getName());
        career.setDescription(request.getDescription());

        return careerMapper.toResponse(careerRepository.save(career));
    }

    @Transactional
    public CareerResponse changeStatus(Long id, CareerStatus status) {
        CareerEntity career = getOrThrow(id);
        career.setStatus(status);
        return careerMapper.toResponse(careerRepository.save(career));
    }

    @Transactional
    public void delete(Long id) {
        CareerEntity career = getOrThrow(id);
        validateHasNoStudyPlans(career);
        validateHasNoStudents(career);
        careerRepository.delete(career);
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private CareerEntity getOrThrow(Long id) {
        return careerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found: " + id));
    }

    private void validateCodeIsUnique(String code) {
        if (careerRepository.existsByCode(code)) {
            throw new BusinessException("A career already exists with code " + code);
        }
    }

    private void validateHasNoStudyPlans(CareerEntity career) {
        if (studyPlanRepository.existsByCareer(career)) {
            throw new BusinessException(
                    "Cannot delete: the career has associated study plans. "
                            + "Only its status can be changed to INACTIVE.");
        }
    }

    private void validateHasNoStudents(CareerEntity career) {
        if (studentRepository.existsByCareer(career)) {
            throw new BusinessException(
                    "Cannot delete: the career has associated students. "
                            + "Only its status can be changed to INACTIVE.");
        }
    }
}
