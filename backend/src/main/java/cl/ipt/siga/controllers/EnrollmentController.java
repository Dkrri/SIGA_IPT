package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.enrollment_dto.AvailableSubjectResponse;
import cl.ipt.siga.dtos.enrollment_dto.EnrollmentRequest;
import cl.ipt.siga.dtos.enrollment_dto.EnrollmentResponse;
import cl.ipt.siga.services.EnrollmentService;
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

/** HTTP adapter for student enrollment. No business logic here. */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Academic enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "List the subjects available for the authenticated student")
    public ResponseEntity<List<AvailableSubjectResponse>> available(
            @RequestParam Long academicPeriodId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                enrollmentService.findAvailableSubjects(currentStudentId(jwt), academicPeriodId));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll the authenticated student in a set of sections")
    public ResponseEntity<List<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        // The student id always comes from the token, never from the body.
        request.setStudentId(currentStudentId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "List the authenticated student's enrollments in a period")
    public ResponseEntity<List<EnrollmentResponse>> my(
            @RequestParam Long academicPeriodId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                enrollmentService.findByStudentAndPeriod(currentStudentId(jwt), academicPeriodId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Cancel one of the authenticated student's enrollments")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal Jwt jwt) {
        enrollmentService.cancel(id, currentStudentId(jwt));
        return ResponseEntity.noContent().build();
    }

    private Long currentStudentId(Jwt jwt) {
        return studentService.findByEmail(jwt.getClaimAsString("email")).getId();
    }
}
