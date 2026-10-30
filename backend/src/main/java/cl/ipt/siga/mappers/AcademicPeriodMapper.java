package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodRequest;
import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import org.springframework.stereotype.Component;

@Component
public class AcademicPeriodMapper {

    /**
     * DTO -> entity.
     * The code is derived from the required naming: year + "-" + semester.
     * The initial status (OPEN) is enforced by the service as a business rule.
     */
    public AcademicPeriodEntity toEntity(AcademicPeriodRequest request) {
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setYear(request.getYear());
        period.setSemester(request.getSemester());
        period.setCode(request.getYear() + "-" + request.getSemester());
        return period;
    }

    public AcademicPeriodResponse toResponse(AcademicPeriodEntity period) {
        AcademicPeriodResponse response = new AcademicPeriodResponse();
        response.setId(period.getId());
        response.setCode(period.getCode());
        response.setYear(period.getYear());
        response.setSemester(period.getSemester());
        response.setStatus(period.getStatus());
        return response;
    }
}
