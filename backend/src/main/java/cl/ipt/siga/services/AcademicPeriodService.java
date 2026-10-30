package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodRequest;
import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.mappers.AcademicPeriodMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Epic 5: academic periods.
 *
 * Rules applied here:
 *  - A period is identified by year + semester (it cannot repeat).
 *  - When created, every period starts in OPEN status.
 *
 * Closing the period (moving to CLOSED) belongs to Epic 7.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;

    @Transactional
    public AcademicPeriodResponse create(AcademicPeriodRequest request) {
        validateYearAndSemesterAreUnique(request.getYear(), request.getSemester());

        AcademicPeriodEntity period = academicPeriodMapper.toEntity(request);
        period.setStatus(AcademicPeriodStatus.OPEN);

        return academicPeriodMapper.toResponse(academicPeriodRepository.save(period));
    }

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> findAll() {
        return academicPeriodRepository.findAll().stream()
                .map(academicPeriodMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicPeriodResponse findById(Long id) {
        return academicPeriodMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AcademicPeriodResponse findByCode(String code) {
        AcademicPeriodEntity period = academicPeriodRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found: " + code));
        return academicPeriodMapper.toResponse(period);
    }

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> findByStatus(AcademicPeriodStatus status) {
        return academicPeriodRepository.findByStatus(status).stream()
                .map(academicPeriodMapper::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private AcademicPeriodEntity getOrThrow(Long id) {
        return academicPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found: " + id));
    }

    private void validateYearAndSemesterAreUnique(Integer year, Integer semester) {
        if (academicPeriodRepository.existsByYearAndSemester(year, semester)) {
            throw new BusinessException(
                    "An academic period already exists for " + year + "-" + semester);
        }
    }
}
