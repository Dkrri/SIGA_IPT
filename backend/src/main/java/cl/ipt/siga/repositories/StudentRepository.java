package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    boolean existsByRun(String run);

    boolean existsByEmail(String email);

    Optional<StudentEntity> findByEmail(String email);

    StudentEntity findByRun(String run);

    List<StudentEntity> findByCareer(CareerEntity career);

    List<StudentEntity> findByStatus(StudentStatus status);

    /** Rule: a career with students is not physically deleted. */
    boolean existsByCareer(CareerEntity career);

    /** Rule: a study plan with students is not physically deleted. */
    boolean existsByStudyPlan(StudyPlanEntity studyPlan);
}
