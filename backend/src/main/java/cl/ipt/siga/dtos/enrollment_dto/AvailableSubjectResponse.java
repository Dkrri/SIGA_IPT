package cl.ipt.siga.dtos.enrollment_dto;

import cl.ipt.siga.dtos.section_dto.ScheduleBlockResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Offered subject the student can enroll in for a period. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSubjectResponse {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Integer semester;

    private Long sectionId;
    private Long teacherId;
    private String teacherName;

    private Integer capacity;
    private long enrolledCount;

    private List<ScheduleBlockResponse> scheduleBlocks = new ArrayList<>();
}
