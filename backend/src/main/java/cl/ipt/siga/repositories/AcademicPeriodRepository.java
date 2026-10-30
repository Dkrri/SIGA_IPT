package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriodEntity, Long> {

    /** Rule: a period is identified by year and semester (it cannot repeat). */
    boolean existsByYearAndSemester(Integer year, Integer semester);

    Optional<AcademicPeriodEntity> findByCode(String code);

    List<AcademicPeriodEntity> findByStatus(AcademicPeriodStatus status);
}
