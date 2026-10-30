package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.matriculation_dto.MatriculationRequest;
import cl.ipt.siga.dtos.matriculation_dto.MatriculationResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.MatriculationEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.mappers.MatriculationMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.MatriculationRepository;
import cl.ipt.siga.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Minimal matriculation service.
 *
 * The administrative matriculation process is OUT OF SCOPE for SIGA IPT.
 * This service exists only to record the information that
 * enrollment needs to validate the "matriculated student" rule.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class MatriculationService {

    private final MatriculationRepository matriculationRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final MatriculationMapper matriculationMapper;

    @Transactional
    public MatriculationResponse create(MatriculationRequest request) {
        StudentEntity student = getStudentOrThrow(request.getStudentId());
        AcademicPeriodEntity period = getPeriodOrThrow(request.getAcademicPeriodId());

        if (matriculationRepository.existsByStudentAndAcademicPeriod(student, period)) {
            throw new BusinessException(
                    "The student is already matriculated in period " + period.getCode());
        }

        MatriculationEntity matriculation = matriculationMapper.toEntity(student, period);
        return matriculationMapper.toResponse(matriculationRepository.save(matriculation));
    }

    @Transactional(readOnly = true)
    public List<MatriculationResponse> findByStudent(Long studentId) {
        StudentEntity student = getStudentOrThrow(studentId);
        return matriculationRepository.findByStudent(student).stream()
                .map(matriculationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculationResponse> findByPeriod(Long academicPeriodId) {
        AcademicPeriodEntity period = getPeriodOrThrow(academicPeriodId);
        return matriculationRepository.findByAcademicPeriod(period).stream()
                .map(matriculationMapper::toResponse)
                .toList();
    }

    private StudentEntity getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    }

    private AcademicPeriodEntity getPeriodOrThrow(Long academicPeriodId) {
        return academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found: " + academicPeriodId));
    }
}
