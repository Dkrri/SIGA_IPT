package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.section_dto.SectionRequest;
import cl.ipt.siga.dtos.section_dto.SectionResponse;
import cl.ipt.siga.services.SectionService;
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

/** HTTP adapter for section management. No business logic here. */
@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "Sections", description = "Section management")
public class SectionController {

    private final SectionService sectionService;
    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a section")
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.create(request));
    }

    @GetMapping
    @Operation(summary = "List sections, optionally filtered by academic period")
    public ResponseEntity<List<SectionResponse>> findAll(
            @RequestParam(required = false) Long academicPeriodId) {
        if (academicPeriodId != null) {
            return ResponseEntity.ok(sectionService.findByAcademicPeriod(academicPeriodId));
        }
        return ResponseEntity.ok(sectionService.findAll());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "List the sections of the authenticated teacher")
    public ResponseEntity<List<SectionResponse>> my(@AuthenticationPrincipal Jwt jwt) {
        Long teacherId = teacherService.findByEmail(jwt.getClaimAsString("email")).getId();
        return ResponseEntity.ok(sectionService.findByTeacher(teacherId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a section by id")
    public ResponseEntity<SectionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a section")
    public ResponseEntity<SectionResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a section")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
