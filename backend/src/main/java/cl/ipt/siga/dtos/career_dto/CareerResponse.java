package cl.ipt.siga.dtos.career_dto;

import cl.ipt.siga.enums.CareerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* DTO - RESPONSE
* Response DTO: output data returned to the client.
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CareerResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private CareerStatus status;
    private Integer semesters;
}
