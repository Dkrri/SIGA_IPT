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

// Subject
public class SubjectEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Subject code
    @Column(nullable = false, length = 20)
    private String code;

    // Name
    @Column(nullable = false, length = 150)
    private String name;

    // Semester from 1 to 4
    @Column(nullable = false)
    private Integer semester;

    // TEL work hours (theory)
    @Column(nullable = false)
    private Integer theoryHours = 0;

    // TEL work hours (exercises)
    @Column(nullable = false)
    private Integer exerciseHours = 0;

    // TEL work hours (lab)
    @Column(nullable = false)
    private Integer labHours = 0;

    // SCT academic credits
    @Column(nullable = false)
    private Integer sct;

    // Study plan it belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlanEntity studyPlan;

    // Self-referencing M:N relation
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_prerequisites",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    // Prerequisites to take the subject
    // Set avoids duplicates
    private Set<SubjectEntity> prerequisites = new HashSet<>();

    // Sections where it is taught
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<SectionEntity> sections = new ArrayList<>();
}
