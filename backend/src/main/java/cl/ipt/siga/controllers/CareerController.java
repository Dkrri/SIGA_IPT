package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.career_dto.CareerRequest;
import cl.ipt.siga.dtos.career_dto.CareerResponse;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.services.CareerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP adapter for career management.
 *
 * Responsibilities:
 *  - Receive HTTP requests (path, query, body).
 *  - Validate request format through @Valid.
 *  - Delegate every operation to CareerService.
 *  - Return the proper HTTP status code.
 *
 * It contains no business logic: rules and transactions live in the service.
 */
@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
@Tag(name = "Careers", description = "Career management")
public class CareerController {

    private final CareerService careerService;

    /** POST /api/careers -> 201 Created. Only ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a career")
    @ApiResponse(responseCode = "201", description = "Career created")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "409", description = "Career code already exists")
    public ResponseEntity<CareerResponse> create(@Valid @RequestBody CareerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(careerService.create(request));
    }

    /** GET /api/careers -> 200 OK. Any authenticated user. */
    @GetMapping
    @Operation(summary = "List all careers")
    public ResponseEntity<List<CareerResponse>> findAll() {
        return ResponseEntity.ok(careerService.findAll());
    }

    /** GET /api/careers/{id} -> 200 OK or 404 Not Found. */
    @GetMapping("/{id}")
    @Operation(summary = "Get a career by id")
    @ApiResponse(responseCode = "200", description = "Career found")
    @ApiResponse(responseCode = "404", description = "Career not found")
    public ResponseEntity<CareerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(careerService.findById(id));
    }

    /** PUT /api/careers/{id} -> 200 OK. Only ADMIN. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a career")
    public ResponseEntity<CareerResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody CareerRequest request) {
        return ResponseEntity.ok(careerService.update(id, request));
    }

    /** PATCH /api/careers/{id}/status?status=INACTIVE -> 200 OK. Only ADMIN. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change the status of a career")
    public ResponseEntity<CareerResponse> changeStatus(@PathVariable Long id,
                                                       @RequestParam CareerStatus status) {
        return ResponseEntity.ok(careerService.changeStatus(id, status));
    }

    /** DELETE /api/careers/{id} -> 204 No Content. Only ADMIN. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a career")
    @ApiResponse(responseCode = "204", description = "Career deleted")
    @ApiResponse(responseCode = "409", description = "Career has study plans or students")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        careerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
