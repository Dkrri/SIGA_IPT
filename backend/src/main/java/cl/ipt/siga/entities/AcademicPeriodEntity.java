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

// Periodo academico
public class AcademicPeriodEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nomenclatura 2027-1 o 2027-2
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    // Año
    @Column(nullable = false)
    private Integer year;

    // Semestre
    @Column(nullable = false)
    private Integer semester;

    // Estado del periodo academico
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademicPeriodStatus status = AcademicPeriodStatus.OPEN;

    // List de secciones
    @OneToMany(mappedBy = "academicPeriod", fetch = FetchType.LAZY)
    private List<SectionEntity> sections = new ArrayList<>();

    // Inscripciones del periodo
    @OneToMany(mappedBy = "academicPeriod", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();

    // Matriculas del periodo
    @OneToMany(mappedBy = "academicPeriod", fetch = FetchType.LAZY)
    private List<MatriculationEntity> matriculations = new ArrayList<>();
}
