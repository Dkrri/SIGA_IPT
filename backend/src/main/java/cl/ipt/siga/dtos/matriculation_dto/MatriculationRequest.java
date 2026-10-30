package cl.ipt.siga.dtos.matriculation_dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Matriculation of a student in a period. The administrative process is
 * out of scope for the system; this DTO exists to record the minimum
 * information that enrollment needs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatriculationRequest {

    @NotNull(message = "Student is required")
    private Long studentId;

    @NotNull(message = "Academic period is required")
    private Long academicPeriodId;
}
