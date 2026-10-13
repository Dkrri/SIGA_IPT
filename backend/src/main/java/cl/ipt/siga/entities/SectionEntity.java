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

// Entidad seccion
public class SectionEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Asignatura de la seccion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    // Periodo en el que se imparte
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    // Docente responsable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    // Maximo de estudiantes 1 a 60
    @Column(nullable = false)
    private Integer capacity;

    // Bloque de horarios
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<ScheduleBlockEntity> scheduleBlocks = new ArrayList<>();

    // Alumnos inscritos
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<EnrollmentEntity> enrollments = new ArrayList<>();
}
