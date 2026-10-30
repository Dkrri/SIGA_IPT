package cl.ipt.siga.dtos.grade_dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Bulk grade upload for a section. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionGradesRequest {

    @NotEmpty(message = "You must submit at least one grade")
    @Valid
    private List<GradeItemRequest> grades;
}
