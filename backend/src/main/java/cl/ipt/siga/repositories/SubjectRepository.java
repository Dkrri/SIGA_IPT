package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {

    /** Rule: code is unique within the study plan. Used on create. */
    boolean existsByStudyPlanAndCode(StudyPlanEntity studyPlan, String code);

    /** Rule: code is unique within the study plan, excluding the subject itself. Used on update. */
    boolean existsByStudyPlanAndCodeAndIdNot(StudyPlanEntity studyPlan, String code, Long id);

    /** Subjects of a study plan, ordered by semester and code. */
    List<SubjectEntity> findByStudyPlanOrderBySemesterAscCodeAsc(StudyPlanEntity studyPlan);
}
