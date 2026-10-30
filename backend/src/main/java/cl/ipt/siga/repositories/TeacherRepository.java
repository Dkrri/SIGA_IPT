package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.TeacherStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    /** Rule: teacher RUN is unique. */
    boolean existsByRun(String run);

    /** Rule: teacher email is unique. */
    boolean existsByEmail(String email);

    Optional<TeacherEntity> findByRun(String run);

    Optional<TeacherEntity> findByEmail(String email);

    List<TeacherEntity> findByStatus(TeacherStatus status);
}
