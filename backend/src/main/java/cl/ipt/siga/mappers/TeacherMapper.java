package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.teacher_dto.TeacherRequest;
import cl.ipt.siga.dtos.teacher_dto.TeacherResponse;
import cl.ipt.siga.entities.TeacherEntity;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper {

    public TeacherEntity toEntity(TeacherRequest request) {
        TeacherEntity teacher = new TeacherEntity();
        teacher.setRun(request.getRun());
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmail(request.getEmail());
        teacher.setProfessionalTitle(request.getProfessionalTitle());
        teacher.setAcademicDegree(request.getAcademicDegree());
        return teacher;
    }

    public TeacherResponse toResponse(TeacherEntity teacher) {
        TeacherResponse response = new TeacherResponse();
        response.setId(teacher.getId());
        response.setRun(teacher.getRun());
        response.setFirstName(teacher.getFirstName());
        response.setLastName(teacher.getLastName());
        response.setEmail(teacher.getEmail());
        response.setProfessionalTitle(teacher.getProfessionalTitle());
        response.setAcademicDegree(teacher.getAcademicDegree());
        response.setStatus(teacher.getStatus());
        return response;
    }
}
