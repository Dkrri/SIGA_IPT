package cl.ipt.siga.dtos.matriculation_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatriculationResponse {

    private Long id;
    private Long studentId;
    private String studentRun;
    private Long academicPeriodId;
    private String academicPeriodCode;
    private LocalDate registeredAt;
}
