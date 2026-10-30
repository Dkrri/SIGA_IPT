package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.grade_dto.GradeRequest;
import cl.ipt.siga.dtos.grade_dto.GradeResponse;
import cl.ipt.siga.dtos.grade_dto.SectionGradesRequest;
import cl.ipt.siga.services.GradeService;
import cl.ipt.siga.services.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for final grade registration. No business logic here. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Final grade registration")
public class GradeController {

    private final GradeService gradeService;
    private final TeacherService teacherService;

    @PutMapping("/enrollments/{enrollmentId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Register or update the grade of one enrollment")
    public ResponseEntity<GradeResponse> registerGrade(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody GradeRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                gradeService.registerGrade(enrollmentId, request, currentTeacherId(jwt)));
    }

    @PutMapping("/sections/{sectionId}/grades")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Register or update the grades of a section")
    public ResponseEntity<List<GradeResponse>> registerGrades(
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionGradesRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                gradeService.registerGrades(sectionId, request, currentTeacherId(jwt)));
    }

    @GetMapping("/sections/{sectionId}/grades")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "List the enrollments and grades of a section")
    public ResponseEntity<List<GradeResponse>> findBySection(
            @PathVariable Long sectionId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(gradeService.findBySection(sectionId, currentTeacherId(jwt)));
    }

    private Long currentTeacherId(Jwt jwt) {
        return teacherService.findByEmail(jwt.getClaimAsString("email")).getId();
    }
}
