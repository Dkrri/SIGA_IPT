package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.enums.CareerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerRepository extends JpaRepository<CareerEntity, Long> {

    /** Rule: career code is unique. */
    boolean existsByCode(String code);

    Optional<CareerEntity> findByCode(String code);

    List<CareerEntity> findByStatus(CareerStatus status);
}
