package cl.ipt.siga.dtos.section_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Section output data. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponse {

    private Long id;

    // Simple subject reference
    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    // Simple period reference
    private Long academicPeriodId;
    private String academicPeriodCode;

    // Simple teacher reference
    private Long teacherId;
    private String teacherName;

    private Integer capacity;

    private List<ScheduleBlockResponse> scheduleBlocks = new ArrayList<>();
}
