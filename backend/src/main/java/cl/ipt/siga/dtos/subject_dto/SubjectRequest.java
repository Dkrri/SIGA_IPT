package cl.ipt.siga.dtos.subject_dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {

    @NotBlank
    @Size(max = 20)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    @Min(value = 1)
    @Max(value = 4)
    private Integer semester;

    @NotNull
    @Min(value = 0)
    private Integer theoryHours = 0;

    @NotNull
    @Min(value = 0)
    private Integer exerciseHours = 0;

    @NotNull
    @Min(value = 0)
    private Integer labHours = 0;

    @NotNull(message = "SCT is required")
    @Min(value = 1)
    @Max(value = 7)
    private Integer sct;

    @NotNull
    private Long studyPlanId;

    @Size(max = 3)
    private Set<Long> prerequisiteIds = new HashSet<>();
}
