package cl.ipt.siga.entities;

import cl.ipt.siga.enums.StudyPlanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"career_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Plan de estudio
public class StudyPlanEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo de plan
    @Column(nullable = false, length = 20)
    private String code;

    // Año del plan
    @Column(nullable = false)
    private Integer year;

    // Estado del plan
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyPlanStatus status = StudyPlanStatus.ACTIVE;

    // Carrera asociada al plan (Muchos planes pertenecen a una carrera)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    // Lista de asignaturas del plan
    @OneToMany(mappedBy = "studyPlan", fetch = FetchType.LAZY)
    private List<SubjectEntity> subjects = new ArrayList<>();
}
