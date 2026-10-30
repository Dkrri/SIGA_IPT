package cl.ipt.siga.dtos.section_dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Input data to create or update a section. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionRequest {

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotNull(message = "Academic period is required")
    private Long academicPeriodId;

    @NotNull(message = "Teacher is required")
    private Long teacherId;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be between 1 and 60")
    @Max(value = 60, message = "Capacity must be between 1 and 60")
    private Integer capacity;

    @NotEmpty(message = "The section must have at least one schedule block")
    @Valid
    private List<ScheduleBlockRequest> scheduleBlocks = new ArrayList<>();
}
