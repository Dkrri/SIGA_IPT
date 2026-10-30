package cl.ipt.siga.dtos.subject_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject output DTO (Epic 3).
 *
 * Entities are not exposed: the plan is represented by id + code, and
 * prerequisites by a summary (id, code, name, semester). This avoids
 * infinite recursion of the self-referencing M:N relation and the problems
 * caused by lazy loading.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private String code;
    private String name;
    private Integer semester;

    private Integer theoryHours;
    private Integer exerciseHours;
    private Integer labHours;

    /** Derived: theoryHours + exerciseHours + labHours. */
    private Integer telTotal;

    private Integer sct;

    // Simple study plan reference
    private Long studyPlanId;
    private String studyPlanCode;

    // Prerequisites as a summary
    private List<SubjectSummaryResponse> prerequisites = new ArrayList<>();
}
