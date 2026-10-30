package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.teacher_dto.TeacherRequest;
import cl.ipt.siga.dtos.teacher_dto.TeacherResponse;
import cl.ipt.siga.dtos.teacher_dto.TeacherUpdateRequest;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.services.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for teacher management. No business logic here. */
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher management")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a teacher")
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all teachers")
    public ResponseEntity<List<TeacherResponse>> findAll() {
        return ResponseEntity.ok(teacherService.findAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get the authenticated teacher")
    public ResponseEntity<TeacherResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(teacherService.findByEmail(jwt.getClaimAsString("email")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a teacher by id")
    public ResponseEntity<TeacherResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a teacher")
    public ResponseEntity<TeacherResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody TeacherUpdateRequest request) {
        return ResponseEntity.ok(teacherService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change the status of a teacher")
    public ResponseEntity<TeacherResponse> changeStatus(@PathVariable Long id,
                                                        @RequestParam TeacherStatus status) {
        return ResponseEntity.ok(teacherService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a teacher")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
