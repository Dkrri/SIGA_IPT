package cl.ipt.siga.entities;

import cl.ipt.siga.enums.AcademicPeriodStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "academic_periods",
        uniqueConstraints = @UniqueConstraint(columnNames = {"year", "semester"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// Academic period
public class AcademicPeriodEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Code format: 2027-1 or 2027-2
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    // Year
    @Column(nullable = false)
    private Integer year;

    // Semester
    @Column(nullable = false)
    private Integer semester;

    // Academic period status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademicPeriodStatus status = AcademicPeriodStatus.OPEN;

    // Sections
    @OneToMany(mappedBy = "academicPeriod", fetch = FetchType.LAZY)
    private List<SectionEntity> sections = new ArrayList<>();

    // Period enrollments
    @OneToMany(mappedBy = "academicPeriod", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();

    // Period matriculations
    @OneToMany(mappedBy = "academicPeriod", fetch = FetchType.LAZY)
    private List<MatriculationEntity> matriculations = new ArrayList<>();
}
