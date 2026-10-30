package cl.ipt.siga.dtos.grade_dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Final grade of an enrollment (range 1.0 - 7.0). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    @NotNull(message = "Grade is required")
    @DecimalMin(value = "1.0", message = "Grade must be between 1.0 and 7.0")
    @DecimalMax(value = "7.0", message = "Grade must be between 1.0 and 7.0")
    private Double finalGrade;
}
