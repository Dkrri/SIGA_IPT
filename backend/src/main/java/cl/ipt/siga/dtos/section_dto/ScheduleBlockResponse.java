package cl.ipt.siga.dtos.section_dto;

import cl.ipt.siga.enums.Weekday;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Schedule block output. `code` is the "L2" representation. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBlockResponse {

    private Weekday weekday;
    private Integer module;
    private String code;
}
