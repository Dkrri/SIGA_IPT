package cl.ipt.siga.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Administrative matriculation of a student in a given academic period.
 * The matriculation process itself is not managed by the system,
 * but this information is needed to validate enrollments
 * in subjects during an academic period.
 */
@Entity
@Table(name = "matriculations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "academic_period_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Matriculation entity
public class MatriculationEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Period
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Matriculation date
    @Column(nullable = false)
    private LocalDate registeredAt;
}
