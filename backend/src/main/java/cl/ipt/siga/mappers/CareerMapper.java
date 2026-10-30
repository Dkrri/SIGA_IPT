package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.career_dto.CareerRequest;
import cl.ipt.siga.dtos.career_dto.CareerResponse;
import cl.ipt.siga.entities.CareerEntity;
import org.springframework.stereotype.Component;

@Component
public class CareerMapper {

    // Build the career entity from the request
    public CareerEntity toEntity(CareerRequest request){
        CareerEntity career = new CareerEntity();
        career.setCode(request.getCode());
        career.setName(request.getName());
        career.setDescription(request.getDescription());
        return career;
    }

    // Build the response from the entity
    public CareerResponse toResponse(CareerEntity career){
        CareerResponse response = new CareerResponse();
        response.setId(career.getId());
        response.setCode(career.getCode());
        response.setName(career.getName());
        response.setDescription(career.getDescription());
        response.setStatus(career.getStatus());
        response.setSemesters(career.getSemesters());
        return response;
    }

}

