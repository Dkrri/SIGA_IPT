package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.MatriculationEntity;
import cl.ipt.siga.entities.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculationRepository extends JpaRepository<MatriculationEntity, Long> {

    /** Epic 6: the student must be matriculated in the period to enroll. */
    boolean existsByStudentAndAcademicPeriod(StudentEntity student, AcademicPeriodEntity period);

    List<MatriculationEntity> findByStudent(StudentEntity student);

    List<MatriculationEntity> findByAcademicPeriod(AcademicPeriodEntity period);
}
