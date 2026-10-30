package cl.ipt.siga.entities;

import cl.ipt.siga.enums.EnrollmentResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
// the same subject cannot be enrolled twice in the same period
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint
        (columnNames = {"student_id", "subject_id", "academic_period_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Enrollment of a specific subject by a student
public class EnrollmentEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Subject
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    // Section in which the student is enrolled
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    // Period
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Enrollment date
    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    // Final grade (1.0 to 7.0); null until registered
    @Column(nullable = true)
    private Double finalGrade;

    // Starts as `PENDING`
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentResult result = EnrollmentResult.PENDING;
}
