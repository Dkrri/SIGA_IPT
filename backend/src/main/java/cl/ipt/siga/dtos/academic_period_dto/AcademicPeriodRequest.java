package cl.ipt.siga.dtos.academic_period_dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Input data to create an academic period.
 * The code ("2027-1") is not sent: it is derived from year + semester.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPeriodRequest {

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year is invalid")
    @Max(value = 2100, message = "Year is invalid")
    private Integer year;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;
}
