package cl.ipt.siga.dtos.academic_period_dto;

import cl.ipt.siga.enums.AcademicPeriodStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Academic period output data. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPeriodResponse {

    private Long id;
    private String code;      // "2027-1"
    private Integer year;
    private Integer semester;
    private AcademicPeriodStatus status;
}
