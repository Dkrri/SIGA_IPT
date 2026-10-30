package cl.ipt.siga.entities;

import cl.ipt.siga.enums.EnrollmentResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Consolidated record created during the academic period closing,
 * to keep the final result in the student's academic history.
 */

@Entity
@Table(name = "academic_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Academic record entity
public class AcademicRecordEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Completed subject
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    // Period in which it was taken
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Final grade
    @Column(nullable = false)
    private Double finalGrade;

    // Approved or failed
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentResult result;

    // Consolidation date
    @Column(nullable = false)
    private LocalDateTime consolidatedAt;
}
