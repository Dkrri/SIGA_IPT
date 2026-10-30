package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.enrollment_dto.AvailableSubjectResponse;
import cl.ipt.siga.dtos.enrollment_dto.EnrollmentResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.enums.EnrollmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class EnrollmentMapper {

    private final SectionMapper sectionMapper;

    public EnrollmentEntity toEntity(StudentEntity student,
                                     SubjectEntity subject,
                                     SectionEntity section,
                                     AcademicPeriodEntity period) {
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setSubject(subject);
        enrollment.setSection(section);
        enrollment.setAcademicPeriod(period);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setResult(EnrollmentResult.PENDING);
        return enrollment;
    }

    public EnrollmentResponse toResponse(EnrollmentEntity enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());

        if (enrollment.getStudent() != null) {
            response.setStudentId(enrollment.getStudent().getId());
            response.setStudentRun(enrollment.getStudent().getRun());
        }
        if (enrollment.getSubject() != null) {
            response.setSubjectId(enrollment.getSubject().getId());
            response.setSubjectCode(enrollment.getSubject().getCode());
            response.setSubjectName(enrollment.getSubject().getName());
        }
        if (enrollment.getSection() != null) {
            response.setSectionId(enrollment.getSection().getId());
        }
        if (enrollment.getAcademicPeriod() != null) {
            response.setAcademicPeriodId(enrollment.getAcademicPeriod().getId());
            response.setAcademicPeriodCode(enrollment.getAcademicPeriod().getCode());
        }

        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setFinalGrade(enrollment.getFinalGrade());
        response.setResult(enrollment.getResult());
        return response;
    }

    /** Offered section -> available-subject DTO for the student. */
    public AvailableSubjectResponse toAvailableSubject(SectionEntity section, long enrolledCount) {
        AvailableSubjectResponse response = new AvailableSubjectResponse();

        if (section.getSubject() != null) {
            response.setSubjectId(section.getSubject().getId());
            response.setSubjectCode(section.getSubject().getCode());
            response.setSubjectName(section.getSubject().getName());
            response.setSemester(section.getSubject().getSemester());
        }
        response.setSectionId(section.getId());

        if (section.getTeacher() != null) {
            response.setTeacherId(section.getTeacher().getId());
            response.setTeacherName(
                    section.getTeacher().getFirstName() + " " + section.getTeacher().getLastName());
        }

        response.setCapacity(section.getCapacity());
        response.setEnrolledCount(enrolledCount);

        if (section.getScheduleBlocks() != null) {
            response.setScheduleBlocks(section.getScheduleBlocks().stream()
                    .sorted(Comparator.comparing(ScheduleBlockEntity::getWeekday)
                            .thenComparing(ScheduleBlockEntity::getModule))
                    .map(sectionMapper::toBlockResponse)
                    .toList());
        }

        return response;
    }
}
