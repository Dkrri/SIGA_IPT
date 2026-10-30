package cl.ipt.siga.dtos.teacher_dto;

import cl.ipt.siga.enums.TeacherStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {

    private Long id;
    private String run;
    private String firstName;
    private String lastName;
    private String email;

    private String professionalTitle;
    private String academicDegree;

    private TeacherStatus status;
}
