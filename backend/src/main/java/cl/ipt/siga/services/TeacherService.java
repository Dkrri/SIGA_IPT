package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.teacher_dto.TeacherRequest;
import cl.ipt.siga.dtos.teacher_dto.TeacherResponse;
import cl.ipt.siga.dtos.teacher_dto.TeacherUpdateRequest;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.mappers.TeacherMapper;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Epic 4: teachers.
 *
 * Rules applied here:
 *  - RUN and email unique.
 *  - On registration, the teacher starts ACTIVE.
 *  - Cannot become INACTIVE while having sections in an OPEN period.
 *  - A teacher with associated sections is not physically deleted.
 *  - Status changes do not alter their information or previous sections.
 *
 * Note: the rule "only ACTIVE teachers are assigned to new sections"
 * belongs to Epic 5 and is validated in SectionService.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final TeacherMapper teacherMapper;

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        validateRunIsUnique(request.getRun());
        validateEmailIsUnique(request.getEmail());

        TeacherEntity teacher = teacherMapper.toEntity(request);
        teacher.setStatus(TeacherStatus.ACTIVE);

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest request) {
        TeacherEntity teacher = getOrThrow(id);

        if (!teacher.getEmail().equals(request.getEmail())) {
            validateEmailIsUnique(request.getEmail());
        }

        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmail(request.getEmail());
        teacher.setProfessionalTitle(request.getProfessionalTitle());
        teacher.setAcademicDegree(request.getAcademicDegree());

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse changeStatus(Long id, TeacherStatus status) {
        TeacherEntity teacher = getOrThrow(id);

        if (status == TeacherStatus.INACTIVE) {
            validateHasNoOpenPeriodSections(teacher);
        }

        teacher.setStatus(status);
        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(Long id) {
        TeacherEntity teacher = getOrThrow(id);
        validateHasNoSections(teacher);
        teacherRepository.delete(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return teacherMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public TeacherResponse findByRun(String run) {
        TeacherEntity teacher = teacherRepository.findByRun(run)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with RUN: " + run));
        return teacherMapper.toResponse(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherResponse findByEmail(String email) {
        TeacherEntity teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teacher not found with email: " + email));
        return teacherMapper.toResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> findByStatus(TeacherStatus status) {
        return teacherRepository.findByStatus(status).stream()
                .map(teacherMapper::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private TeacherEntity getOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
    }

    private void validateRunIsUnique(String run) {
        if (teacherRepository.existsByRun(run)) {
            throw new BusinessException("A teacher with that RUN already exists");
        }
    }

    private void validateEmailIsUnique(String email) {
        if (teacherRepository.existsByEmail(email)) {
            throw new BusinessException("A teacher with that email already exists");
        }
    }

    /** Rule: a teacher with associated sections is not physically deleted. */
    private void validateHasNoSections(TeacherEntity teacher) {
        if (sectionRepository.existsByTeacher(teacher)) {
            throw new BusinessException(
                    "Cannot delete: the teacher has associated sections");
        }
    }

    /** Rule: cannot become INACTIVE with sections in an OPEN period. */
    private void validateHasNoOpenPeriodSections(TeacherEntity teacher) {
        if (sectionRepository.existsByTeacherAndAcademicPeriodStatus(
                teacher, AcademicPeriodStatus.OPEN)) {
            throw new BusinessException(
                    "Cannot deactivate: the teacher has sections in an OPEN period");
        }
    }
}
