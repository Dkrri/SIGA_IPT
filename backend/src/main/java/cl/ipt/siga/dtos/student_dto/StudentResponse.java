package cl.ipt.siga.dtos.student_dto;

import cl.ipt.siga.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String run;
    private String firstName;
    private String lastName;
    private String email;

    // Career reference
    private Long careerId;
    private String careerName;

    // Study plan reference
    private Long studyPlanId;
    private String studyPlanCode;

    private StudentStatus status;
}
