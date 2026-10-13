package cl.ipt.siga.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Representa la matrícula administrativa de un estudiante en un período académico determinado.
 * NO se gestiona el proceso de matrícula en sí,
 * pero se necesita esta información para validar las inscripciones
 * a cursos durante un período académico.
 */
@Entity
@Table(name = "matriculations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "academic_period_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Entidad matricula
public class MatriculationEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del estudiante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Periodo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Fecha de la matricula
    @Column(nullable = false)
    private LocalDate registeredAt;
}
