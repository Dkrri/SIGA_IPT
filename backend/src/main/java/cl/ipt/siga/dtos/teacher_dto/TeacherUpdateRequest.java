package cl.ipt.siga.dtos.teacher_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data to update a teacher.
 * Does not include RUN: it is the teacher's identity and is not modified.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Professional title is required")
    @Size(max = 150, message = "Professional title cannot exceed 150 characters")
    private String professionalTitle;

    @NotBlank(message = "Academic degree is required")
    @Size(max = 150, message = "Academic degree cannot exceed 150 characters")
    private String academicDegree;
}
