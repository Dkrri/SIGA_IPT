package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.grade_dto.GradeResponse;
import cl.ipt.siga.entities.EnrollmentEntity;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeResponse toResponse(EnrollmentEntity enrollment) {
        GradeResponse response = new GradeResponse();
        response.setEnrollmentId(enrollment.getId());

        if (enrollment.getStudent() != null) {
            response.setStudentId(enrollment.getStudent().getId());
            response.setStudentRun(enrollment.getStudent().getRun());
            response.setStudentName(
                    (enrollment.getStudent().getFirstName() + " "
                            + enrollment.getStudent().getLastName()).trim());
        }
        if (enrollment.getSubject() != null) {
            response.setSubjectId(enrollment.getSubject().getId());
            response.setSubjectCode(enrollment.getSubject().getCode());
            response.setSubjectName(enrollment.getSubject().getName());
        }

        response.setFinalGrade(enrollment.getFinalGrade());
        response.setResult(enrollment.getResult());
        return response;
    }
}
