package cl.ipt.siga.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "subjects",
        uniqueConstraints = @UniqueConstraint(columnNames = {"study_plan_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Asignatura
public class SubjectEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo de la asignatura
    @Column(nullable = false, length = 20)
    private String code;

    // Nombre
    @Column(nullable = false, length = 150)
    private String name;

    // Semestre de 1 a 4
    @Column(nullable = false)
    private Integer semester;

    // Horas de trabajo TEL (teoria)
    @Column(nullable = false)
    private Integer theoryHours = 0;

    // Horas de trabajo TEL (ejercicio)
    @Column(nullable = false)
    private Integer exerciseHours = 0;

    // Horas de trabajo TEL (lab)
    @Column(nullable = false)
    private Integer labHours = 0;

    // Creditos academicos SCT ()
    @Column(nullable = false)
    private Integer sct;

    // Plan de estudios al que pertenece
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlanEntity studyPlan;

    // Relacion N:M consigo misma
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_prerequisites",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    // Prerrequisitos para rendir la Asignatura
    // set evita duplicados
    private Set<SubjectEntity> prerequisites = new HashSet<>();

    // Lista de secciones en donde se imparte
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<SectionEntity> sections = new ArrayList<>();
}
