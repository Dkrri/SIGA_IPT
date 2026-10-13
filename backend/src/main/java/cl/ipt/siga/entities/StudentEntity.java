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

// Estudiante
public class StudentEntity extends UserProfileEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Carrera del estudiante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    // Plan al que ingreso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlanEntity studyPlan;

    // Estado del estudiante
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.REGULAR;

    // Matriculas
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<MatriculationEntity> matriculations = new ArrayList<>();

    // Asignaturas inscritas
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();
}
