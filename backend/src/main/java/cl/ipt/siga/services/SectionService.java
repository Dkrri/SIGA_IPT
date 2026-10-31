package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;

import cl.ipt.siga.dtos.section_dto.ScheduleBlockRequest;
import cl.ipt.siga.dtos.section_dto.SectionRequest;
import cl.ipt.siga.dtos.section_dto.SectionResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.mappers.SectionMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import cl.ipt.siga.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic for Epic 5: sections.
 *
 * Rules applied here:
 *  - A section is linked to an existing subject and period.
 *  - A subject can have only one section per period.
 *  - The responsible teacher must be ACTIVE.
 *  - Capacity between 1 and 60.
 *  - At least one schedule block, without duplicates.
 *  - Number of blocks = total TEL / 2.
 *  - A teacher cannot have two sections at the same weekday + module (same period).
 *  - A section with enrolled students is not deleted.
 *
 * Rejections are handled with BusinessException / ResourceNotFoundException.
 */
@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionMapper sectionMapper;

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    @Transactional
    public SectionResponse create(SectionRequest request) {
        SubjectEntity subject = getSubjectOrThrow(request.getSubjectId());
        AcademicPeriodEntity period = getPeriodOrThrow(request.getAcademicPeriodId());
        TeacherEntity teacher = getTeacherOrThrow(request.getTeacherId());

        validateTeacherIsActive(teacher);
        validateCapacity(request.getCapacity());
        validateSectionIsUnique(subject, period);
        validateBlocks(subject, request.getScheduleBlocks());
        validateTeacherSchedule(teacher, period, request.getScheduleBlocks(), null);

        SectionEntity section = sectionMapper.toEntity(request, subject, period, teacher);
        return toResponse(sectionRepository.save(section));
    }

    @Transactional
    public SectionResponse update(Long id, SectionRequest request) {
        SectionEntity section = getOrThrow(id);

        SubjectEntity subject = getSubjectOrThrow(request.getSubjectId());
        AcademicPeriodEntity period = getPeriodOrThrow(request.getAcademicPeriodId());
        TeacherEntity teacher = getTeacherOrThrow(request.getTeacherId());

        validateTeacherIsActive(teacher);
        validateCapacity(request.getCapacity());
        validateSectionIsUniqueForUpdate(subject, period, id);
        validateBlocks(subject, request.getScheduleBlocks());
        validateTeacherSchedule(teacher, period, request.getScheduleBlocks(), id);

        section.setSubject(subject);
        section.setAcademicPeriod(period);
        section.setTeacher(teacher);
        section.setCapacity(request.getCapacity());

        // orphanRemoval deletes the previous blocks
        section.getScheduleBlocks().clear();
        for (ScheduleBlockRequest blockRequest : request.getScheduleBlocks()) {
            ScheduleBlockEntity block = new ScheduleBlockEntity();
            block.setWeekday(blockRequest.getWeekday());
            block.setModule(blockRequest.getModule());
            block.setSection(section);
            section.getScheduleBlocks().add(block);
        }

        return toResponse(sectionRepository.save(section));
    }

    @Transactional
    public void delete(Long id) {
        SectionEntity section = getOrThrow(id);
        validateHasNoEnrollments(section);
        sectionRepository.delete(section);
    }

    // ------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SectionResponse> findAll() {
        return sectionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SectionResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findByAcademicPeriod(Long academicPeriodId) {
        AcademicPeriodEntity period = getPeriodOrThrow(academicPeriodId);
        return sectionRepository.findByAcademicPeriod(period).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findByTeacher(Long teacherId) {
        TeacherEntity teacher = getTeacherOrThrow(teacherId);
        return sectionRepository.findByTeacher(teacher).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Adds the current enrollment count to the section DTO. */
    private SectionResponse toResponse(SectionEntity section) {
        SectionResponse response = sectionMapper.toResponse(section);
        response.setEnrolledCount((int) enrollmentRepository.countBySection(section));
        return response;
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    private SectionEntity getOrThrow(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + id));
    }

    private SubjectEntity getSubjectOrThrow(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
    }

    private AcademicPeriodEntity getPeriodOrThrow(Long academicPeriodId) {
        return academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found: " + academicPeriodId));
    }

    private TeacherEntity getTeacherOrThrow(Long teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));
    }

    private void validateTeacherIsActive(TeacherEntity teacher) {
        if (teacher.getStatus() != TeacherStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE teachers can be assigned to sections");
        }
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity < 1 || capacity > 60) {
            throw new BusinessException("Capacity must be between 1 and 60");
        }
    }

    private void validateSectionIsUnique(SubjectEntity subject, AcademicPeriodEntity period) {
        if (sectionRepository.existsBySubjectAndAcademicPeriod(subject, period)) {
            throw new BusinessException(
                    "A section already exists for subject " + subject.getCode()
                            + " in period " + period.getCode());
        }
    }

    private void validateSectionIsUniqueForUpdate(SubjectEntity subject,
                                                  AcademicPeriodEntity period,
                                                  Long sectionId) {
        if (sectionRepository.existsBySubjectAndAcademicPeriodAndIdNot(subject, period, sectionId)) {
            throw new BusinessException(
                    "Another section already exists for subject " + subject.getCode()
                            + " in period " + period.getCode());
        }
    }

    private void validateBlocks(SubjectEntity subject, List<ScheduleBlockRequest> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            throw new BusinessException("The section must have at least one schedule block");
        }
        validateNoDuplicateBlocks(blocks);
        validateBlockCount(subject, blocks);
    }

    private void validateNoDuplicateBlocks(List<ScheduleBlockRequest> blocks) {
        Set<String> seen = new HashSet<>();
        for (ScheduleBlockRequest block : blocks) {
            String key = block.getWeekday() + "-" + block.getModule();
            if (!seen.add(key)) {
                throw new BusinessException("Duplicate schedule block: " + key);
            }
        }
    }

    private void validateBlockCount(SubjectEntity subject, List<ScheduleBlockRequest> blocks) {
        int telTotal = subject.getTheoryHours()
                + subject.getExerciseHours()
                + subject.getLabHours();

        if (telTotal % 2 != 0) {
            throw new BusinessException("Total TEL hours must be even to form 2-hour blocks");
        }
        int required = telTotal / 2;

        if (blocks.size() != required) {
            throw new BusinessException(
                    "The section must have " + required + " blocks (total TEL / 2)");
        }
    }

    private void validateTeacherSchedule(TeacherEntity teacher,
                                        AcademicPeriodEntity period,
                                        List<ScheduleBlockRequest> newBlocks,
                                        Long currentSectionId) {
        List<SectionEntity> teacherSections =
                sectionRepository.findByTeacherAndAcademicPeriod(teacher, period);

        for (SectionEntity section : teacherSections) {
            if (currentSectionId != null && section.getId().equals(currentSectionId)) {
                continue;
            }
            for (ScheduleBlockEntity existing : section.getScheduleBlocks()) {
                for (ScheduleBlockRequest candidate : newBlocks) {
                    if (existing.getWeekday() == candidate.getWeekday()
                            && existing.getModule().equals(candidate.getModule())) {
                        throw new BusinessException(
                                "The teacher already has a section in block "
                                        + candidate.getWeekday().getCode() + candidate.getModule());
                    }
                }
            }
        }
    }

    private void validateHasNoEnrollments(SectionEntity section) {
        if (enrollmentRepository.existsBySection(section)) {
            throw new BusinessException(
                    "Cannot delete: the section has enrolled students");
        }
    }
}
