package cl.ipt.siga.entities;

import cl.ipt.siga.enums.EnrollmentResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
// no se puede inscribir la misma asignatura dos veces en el mismo período
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint
        (columnNames = {"student_id", "subject_id", "academic_period_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Es la inscripción de una asignatura concreta por parte de un estudiante
public class EnrollmentEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del estudiante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Asignatura
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    // Seccion donde se inscribe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    // Periodo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Fecha de la inscripcion
    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    // Nota final (1.0 a 7.0) puede ser null hasta que se registre
    @Column(nullable = true)
    private Double finalGrade;

    // Nace `PENDING`
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentResult result = EnrollmentResult.PENDING;
}
