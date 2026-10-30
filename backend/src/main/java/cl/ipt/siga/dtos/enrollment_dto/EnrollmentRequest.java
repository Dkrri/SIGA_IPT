package cl.ipt.siga.dtos.enrollment_dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Bulk enrollment: the student sends every section they want to take.
 * Done in one operation so the minimum of 3 subjects can be validated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    /**
     * Set by the controller from the authenticated user's JWT.
     * It is not required from the client, to avoid impersonation.
     */
    private Long studentId;

    @NotEmpty(message = "You must select at least one section")
    @Size(max = 6, message = "You cannot enroll in more than 6 subjects per period")
    private List<Long> sectionIds;
}
