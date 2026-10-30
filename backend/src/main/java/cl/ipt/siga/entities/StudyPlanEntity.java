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

// Study plan
public class StudyPlanEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Study plan code
    @Column(nullable = false, length = 20)
    private String code;

    // Study plan year
    @Column(nullable = false)
    private Integer year;

    // Study plan status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyPlanStatus status = StudyPlanStatus.ACTIVE;

    // Career associated with the plan (many plans belong to one career)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    // Study plan subjects
    @OneToMany(mappedBy = "studyPlan", fetch = FetchType.LAZY)
    private List<SubjectEntity> subjects = new ArrayList<>();
}
