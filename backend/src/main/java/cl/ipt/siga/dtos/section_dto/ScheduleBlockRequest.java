package cl.ipt.siga.dtos.section_dto;

import cl.ipt.siga.enums.Weekday;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Schedule block of a section: weekday + module. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBlockRequest {

    @NotNull(message = "Weekday is required")
    private Weekday weekday;

    @NotNull(message = "Module is required")
    @Min(value = 1, message = "Module must be between 1 and 6")
    @Max(value = 6, message = "Module must be between 1 and 6")
    private Integer module;
}
