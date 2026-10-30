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
public class TeacherEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RUN unique in the system
    @Column(nullable = false, unique = true, length = 20)
    private String run;

    // Names
    @Column(nullable = false, length = 100)
    private String firstName;

    // Last name
    @Column(nullable = false, length = 100)
    private String lastName;

    // Email unique in the system
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Professional title
    @Column(nullable = false, length = 150)
    private String professionalTitle;

    // Academic degree
    @Column(nullable = false, length = 150)
    private String academicDegree;

    // Teacher status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherStatus status = TeacherStatus.ACTIVE;

    // Sections taught by the teacher
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<SectionEntity> sections = new ArrayList<>();
}
