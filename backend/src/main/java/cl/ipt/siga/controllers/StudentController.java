package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.student_dto.StudentRequest;
import cl.ipt.siga.dtos.student_dto.StudentResponse;
import cl.ipt.siga.dtos.student_dto.StudentUpdateRequest;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.services.StudentService;
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

/** HTTP adapter for student management. No business logic here. */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a student")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all students")
    public ResponseEntity<List<StudentResponse>> findAll() {
        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get the authenticated student")
    public ResponseEntity<StudentResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(studentService.findByEmail(jwt.getClaimAsString("email")));
    }

    @GetMapping("/by-run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a student by RUN")
    public ResponseEntity<StudentResponse> findByRun(@RequestParam String run) {
        return ResponseEntity.ok(studentService.findByRun(run));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a student by id")
    public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a student")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change the status of a student")
    public ResponseEntity<StudentResponse> changeStatus(@PathVariable Long id,
                                                        @RequestParam StudentStatus status) {
        return ResponseEntity.ok(studentService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a student")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
