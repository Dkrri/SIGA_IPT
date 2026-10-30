package cl.ipt.siga.dtos.closing_dto;

import cl.ipt.siga.enums.AcademicPeriodStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Result of closing an academic period. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClosingResponse {

    private Long academicPeriodId;
    private String academicPeriodCode;
    private AcademicPeriodStatus status;

    private int consolidatedEnrollments;
    private int graduatedStudents;
    private int eliminatedStudents;
}
