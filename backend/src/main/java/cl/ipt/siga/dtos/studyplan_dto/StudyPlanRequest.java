package cl.ipt.siga.dtos.studyplan_dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanRequest {

    @NotBlank(message = "Study plan code is required")
    @Size(max = 20, message = "Code cannot exceed 20 characters")
    private String code;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year is invalid")
    @Max(value = 2100, message = "Year is invalid")
    private Integer year;

    @NotNull(message = "Career is required")
    private Long careerId;

    /** If true, this plan becomes the current one for the career. */
    private boolean current;
}
