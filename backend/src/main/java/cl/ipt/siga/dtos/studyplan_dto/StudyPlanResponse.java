package cl.ipt.siga.dtos.studyplan_dto;

import cl.ipt.siga.enums.StudyPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanResponse {

    private Long id;
    private String code;
    private Integer year;
    private StudyPlanStatus status;

    // Simple career reference (the entity is not exposed)
    private Long careerId;
    private String careerName;
}
