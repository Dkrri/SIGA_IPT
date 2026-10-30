package cl.ipt.siga.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "academic_period_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Section entity
public class SectionEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Section subject
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    // Period in which it is taught
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Responsible teacher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    // Maximum number of students: 1 to 60
    @Column(nullable = false)
    private Integer capacity;

    // Schedule blocks
    // cascade + orphanRemoval: saving the section saves its blocks,
    // and replacing them on update removes the previous ones.
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleBlockEntity> scheduleBlocks = new ArrayList<>();

    // Enrolled students
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();
}
