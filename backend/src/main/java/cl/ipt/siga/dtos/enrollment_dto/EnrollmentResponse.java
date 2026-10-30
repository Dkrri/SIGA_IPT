package cl.ipt.siga.dtos.enrollment_dto;

import cl.ipt.siga.enums.EnrollmentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Enrollment output data. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long id;

    private Long studentId;
    private String studentRun;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    private Long sectionId;

    private Long academicPeriodId;
    private String academicPeriodCode;

    private LocalDateTime enrolledAt;
    private Double finalGrade;
    private EnrollmentResult result;
}
