package cl.ipt.siga.dtos.error_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Standard error payload returned by every endpoint when something fails. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
