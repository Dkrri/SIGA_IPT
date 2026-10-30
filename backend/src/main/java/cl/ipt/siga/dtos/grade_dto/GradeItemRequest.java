package cl.ipt.siga.dtos.grade_dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A grade inside a bulk upload for a section. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeItemRequest {

    @NotNull(message = "Enrollment is required")
    private Long enrollmentId;

    @NotNull(message = "Grade is required")
    @DecimalMin(value = "1.0", message = "Grade must be between 1.0 and 7.0")
    @DecimalMax(value = "7.0", message = "Grade must be between 1.0 and 7.0")
    private Double finalGrade;
}
