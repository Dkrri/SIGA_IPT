package cl.ipt.siga.entities;

import cl.ipt.siga.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Student
public class StudentEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RUN unique in the system
    @Column(nullable = false, unique = true, length = 20)
    private String run;

    // Names
    @Column(nullable = false, length = 100)
    private String firstName;

    // Last name
    @Column(nullable = false, length = 100)
    private String lastName;

    // Email unique in the system
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Student's career
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    // Study plan the student entered with
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlanEntity studyPlan;

    // Student status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.REGULAR;

    // Matriculations
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<MatriculationEntity> matriculations = new ArrayList<>();

    // Enrolled subjects
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();
}
