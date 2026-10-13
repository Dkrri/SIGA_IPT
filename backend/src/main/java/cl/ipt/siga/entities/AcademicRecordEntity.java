package cl.ipt.siga.entities;

import cl.ipt.siga.enums.EnrollmentResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro consolidado que se crea durante el cierre del período académico,
 * para conservar el resultado definitivo en el historial del estudiante.
 */

@Entity
@Table(name = "academic_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Entidad Historial académico
public class AcademicRecordEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del estudiante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Asignatura cursada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    // Periodo en el que se curso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Nota final
    @Column(nullable = false)
    private Double finalGrade;

    // Aprobado o Reprobado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentResult result;

    // Fecha en que se consolido
    @Column(nullable = false)
    private LocalDateTime consolidatedAt;
}
