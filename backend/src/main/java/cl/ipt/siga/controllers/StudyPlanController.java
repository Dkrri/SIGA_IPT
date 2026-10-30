package cl.ipt.siga.controllers;

import cl.ipt.siga.dtos.studyplan_dto.StudyPlanRequest;
import cl.ipt.siga.dtos.studyplan_dto.StudyPlanResponse;
import cl.ipt.siga.services.StudyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for study plan management. No business logic here. */
@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
@Tag(name = "Study Plans", description = "Study plan management")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a study plan")
    public ResponseEntity<StudyPlanResponse> create(@Valid @RequestBody StudyPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyPlanService.create(request));
    }

    @GetMapping
    @Operation(summary = "List study plans of a career")
    public ResponseEntity<List<StudyPlanResponse>> findByCareer(@RequestParam Long careerId) {
        return ResponseEntity.ok(studyPlanService.findByCareer(careerId));
    }

    @GetMapping("/current")
    @Operation(summary = "Get the current study plan of a career")
    public ResponseEntity<StudyPlanResponse> findCurrent(@RequestParam Long careerId) {
        return ResponseEntity.ok(studyPlanService.findCurrent(careerId));
    }

    @PatchMapping("/{id}/current")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set a study plan as current")
    public ResponseEntity<StudyPlanResponse> setCurrent(@PathVariable Long id) {
        return ResponseEntity.ok(studyPlanService.setCurrent(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a study plan")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studyPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
