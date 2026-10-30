package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.subject_dto.SubjectRequest;
import cl.ipt.siga.dtos.subject_dto.SubjectResponse;
import cl.ipt.siga.dtos.subject_dto.SubjectSummaryResponse;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;

@Component
public class SubjectMapper {

    public SubjectEntity toEntity(SubjectRequest request, StudyPlanEntity plan, Set<SubjectEntity> prerequisites) {
        SubjectEntity subject = new SubjectEntity();
        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setSemester(request.getSemester());
        subject.setTheoryHours(request.getTheoryHours());
        subject.setExerciseHours(request.getExerciseHours());
        subject.setLabHours(request.getLabHours());
        subject.setSct(request.getSct());
        subject.setStudyPlan(plan);
        subject.setPrerequisites(prerequisites);
        return subject;
    }

    public SubjectResponse toResponse(SubjectEntity subject) {
        SubjectResponse response = new SubjectResponse();

        response.setId(subject.getId());
        response.setCode(subject.getCode());
        response.setName(subject.getName());
        response.setSemester(subject.getSemester());

        response.setTheoryHours(subject.getTheoryHours());
        response.setExerciseHours(subject.getExerciseHours());
        response.setLabHours(subject.getLabHours());
        response.setTelTotal(
                subject.getTheoryHours()
                        + subject.getExerciseHours()
                        + subject.getLabHours());

        response.setSct(subject.getSct());

        if (subject.getStudyPlan() != null) {
            response.setStudyPlanId(subject.getStudyPlan().getId());
            response.setStudyPlanCode(subject.getStudyPlan().getCode());
        }

        if (subject.getPrerequisites() != null) {
            response.setPrerequisites(subject.getPrerequisites().stream()
                    .sorted(Comparator.comparing(SubjectEntity::getSemester)
                            .thenComparing(SubjectEntity::getCode))
                    .map(this::toSummary)
                    .toList());
        }

        return response;
    }

    public SubjectSummaryResponse toSummary(SubjectEntity subject) {
        return new SubjectSummaryResponse(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getSemester()
        );
    }
}
