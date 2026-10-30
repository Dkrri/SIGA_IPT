package cl.ipt.siga.dtos.career_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* DTO - REQUEST
 * DTO with the data sent by the client.
 * The entity is never received directly.
 *
 * Validates format.
 */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CareerRequest {

    @NotBlank
    @Size(max = 20)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String description;


}
