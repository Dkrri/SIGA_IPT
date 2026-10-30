package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.matriculation_dto.MatriculationRequest;
import cl.ipt.siga.dtos.matriculation_dto.MatriculationResponse;
import cl.ipt.siga.services.MatriculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP adapter for the minimal matriculation record.
 * The administrative matriculation process is out of scope for SIGA IPT;
 * this only stores the information enrollment needs.
 */
@RestController
@RequestMapping("/api/matriculations")
@RequiredArgsConstructor
@Tag(name = "Matriculations", description = "Student matriculation per academic period")
public class MatriculationController {

    private final MatriculationService matriculationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a student as matriculated in a period")
    public ResponseEntity<MatriculationResponse> create(
            @Valid @RequestBody MatriculationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matriculationService.create(request));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List the matriculations of a student")
    public ResponseEntity<List<MatriculationResponse>> findByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(matriculationService.findByStudent(studentId));
    }

    @GetMapping("/period/{academicPeriodId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List the matriculations of an academic period")
    public ResponseEntity<List<MatriculationResponse>> findByPeriod(
            @PathVariable Long academicPeriodId) {
        return ResponseEntity.ok(matriculationService.findByPeriod(academicPeriodId));
    }
}
