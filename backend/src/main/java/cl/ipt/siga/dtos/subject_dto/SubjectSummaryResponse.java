package cl.ipt.siga.dtos.subject_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subject summary, used to represent prerequisites without
 * exposing the entity or embedding its full structure again.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectSummaryResponse {

    private Long id;
    private String code;
    private String name;
    private Integer semester;
}
