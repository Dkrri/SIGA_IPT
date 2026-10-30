package cl.ipt.siga.entities;

import cl.ipt.siga.enums.CareerStatus;

import jakarta.persistence.*; // Persists Java objects in a relational database
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "careers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Career entity
public class CareerEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Code
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // Name
    @Column(nullable = false, length = 100)
    private String name;

    // Description
    @Column(nullable = false, length = 500)
    private String description;

    // Career status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length =20)
    private CareerStatus status = CareerStatus.ACTIVE;

    // Semesters
    @Column(nullable = false)
    private Integer semesters = 4;

    // Study plans of the career
    @OneToMany(mappedBy = "career", fetch = FetchType.LAZY)
    private List<StudyPlanEntity> studyPlans = new ArrayList<>();

}
