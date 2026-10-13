package cl.ipt.siga.entities;

import cl.ipt.siga.enums.TeacherStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEntity extends UserProfileEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titulo profesional
    @Column(nullable = false, length = 150)
    private String professionalTitle;

    // Grado academico
    @Column(nullable = false, length = 150)
    private String academicDegree;

    // Estado del docente
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherStatus status = TeacherStatus.ACTIVE;

    // Lista de secciones donde imparte clases
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<SectionEntity> sections = new ArrayList<>();
}
