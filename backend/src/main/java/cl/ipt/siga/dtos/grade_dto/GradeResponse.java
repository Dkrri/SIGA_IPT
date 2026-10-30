package cl.ipt.siga.dtos.grade_dto;

import cl.ipt.siga.enums.EnrollmentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Output data of a registered grade. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {

    private Long enrollmentId;

    private Long studentId;
    private String studentRun;
    private String studentName;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    private Double finalGrade;
    private EnrollmentResult result;
}
