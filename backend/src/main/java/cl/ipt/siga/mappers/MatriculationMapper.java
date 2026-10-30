package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.matriculation_dto.MatriculationResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.MatriculationEntity;
import cl.ipt.siga.entities.StudentEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MatriculationMapper {

    public MatriculationEntity toEntity(StudentEntity student, AcademicPeriodEntity period) {
        MatriculationEntity matriculation = new MatriculationEntity();
        matriculation.setStudent(student);
        matriculation.setAcademicPeriod(period);
        matriculation.setRegisteredAt(LocalDate.now());
        return matriculation;
    }

    public MatriculationResponse toResponse(MatriculationEntity matriculation) {
        MatriculationResponse response = new MatriculationResponse();
        response.setId(matriculation.getId());

        if (matriculation.getStudent() != null) {
            response.setStudentId(matriculation.getStudent().getId());
            response.setStudentRun(matriculation.getStudent().getRun());
        }
        if (matriculation.getAcademicPeriod() != null) {
            response.setAcademicPeriodId(matriculation.getAcademicPeriod().getId());
            response.setAcademicPeriodCode(matriculation.getAcademicPeriod().getCode());
        }

        response.setRegisteredAt(matriculation.getRegisteredAt());
        return response;
    }
}
