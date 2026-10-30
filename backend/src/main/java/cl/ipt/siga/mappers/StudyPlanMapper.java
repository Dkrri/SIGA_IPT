package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.studyplan_dto.StudyPlanRequest;
import cl.ipt.siga.dtos.studyplan_dto.StudyPlanResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanMapper {

    public StudyPlanEntity toEntity(StudyPlanRequest request, CareerEntity career) {
        StudyPlanEntity plan = new StudyPlanEntity();
        plan.setCode(request.getCode());
        plan.setYear(request.getYear());
        plan.setCareer(career);
        return plan;
    }

    public StudyPlanResponse toResponse(StudyPlanEntity plan) {
        StudyPlanResponse response = new StudyPlanResponse();
        response.setId(plan.getId());
        response.setCode(plan.getCode());
        response.setYear(plan.getYear());
        response.setStatus(plan.getStatus());

        if (plan.getCareer() != null) {
            response.setCareerId(plan.getCareer().getId());
            response.setCareerName(plan.getCareer().getName());
        }
        return response;
    }
}
