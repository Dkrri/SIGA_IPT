package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.student_dto.StudentRequest;
import cl.ipt.siga.dtos.student_dto.StudentResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
    /* DTO -> entity.
     * Career and study plan are parameters because the SERVICE
     * already resolved them (the mapper does not decide relations).
     */
    public StudentEntity toEntity(StudentRequest request, CareerEntity career, StudyPlanEntity studyPlan) {
        StudentEntity student = new StudentEntity();
        student.setRun(request.getRun());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setCareer(career);
        student.setStudyPlan(studyPlan);
        return student;
    }

    /* Entity -> DTO.
     * Relations (career, studyPlan) are accessed inside the transaction
     * of the service, so there are no Lazy issues here.
     */
    public StudentResponse toResponse(StudentEntity student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setRun(student.getRun());
        response.setFirstName(student.getFirstName());
        response.setLastName(student.getLastName());
        response.setEmail(student.getEmail());
        response.setStatus(student.getStatus());

        if (student.getCareer() != null) {
            response.setCareerId(student.getCareer().getId());
            response.setCareerName(student.getCareer().getName());
        }

        if (student.getStudyPlan() != null) {
            response.setStudyPlanId(student.getStudyPlan().getId());
            response.setStudyPlanCode(student.getStudyPlan().getCode());
        }
        return response;
    }
}
