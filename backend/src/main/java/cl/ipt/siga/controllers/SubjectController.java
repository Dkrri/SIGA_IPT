package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.subject_dto.SubjectRequest;
import cl.ipt.siga.dtos.subject_dto.SubjectResponse;
import cl.ipt.siga.services.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for subject management. No business logic here. */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject management")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a subject")
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @GetMapping
    @Operation(summary = "List subjects, optionally filtered by study plan")
    public ResponseEntity<List<SubjectResponse>> findAll(
            @RequestParam(required = false) Long studyPlanId) {
        if (studyPlanId != null) {
            return ResponseEntity.ok(subjectService.findByStudyPlan(studyPlanId));
        }
        return ResponseEntity.ok(subjectService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a subject by id")
    public ResponseEntity<SubjectResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a subject")
    public ResponseEntity<SubjectResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a subject")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
