package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.enums.StudyPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlanEntity, Long> {

    /** Rule: study plan code is unique within the career. */
    boolean existsByCareerAndCode(CareerEntity career, String code);

    /** Rule: only one study plan can be current (ACTIVE) per career. */
    Optional<StudyPlanEntity> findByCareerAndStatus(CareerEntity career, StudyPlanStatus status);

    List<StudyPlanEntity> findByCareerOrderByYearDesc(CareerEntity career);

    /** Rule: a career with study plans is not physically deleted. */
    boolean existsByCareer(CareerEntity career);
}
