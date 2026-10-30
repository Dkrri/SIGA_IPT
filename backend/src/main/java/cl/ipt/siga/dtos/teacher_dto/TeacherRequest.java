package cl.ipt.siga.dtos.teacher_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequest {

    @NotBlank(message = "RUN is required")
    @Size(max = 20, message = "RUN cannot exceed 20 characters")
    private String run;

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

    @NotBlank
    private String professionalTitle;

    @NotBlank
    private String academicDegree;
}
