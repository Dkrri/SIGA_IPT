package cl.ipt.siga.entities;

import cl.ipt.siga.enums.CareerStatus;

import jakarta.persistence.*; //Guarda objetos Java en una base de datos relacional
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

// Entidad Carrera
public class CareerEntity {

    // Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // Nombre
    @Column(nullable = false, length = 100)
    private String name;

    // Descripcion
    @Column(nullable = false, length = 500)
    private String description;

    // Estado de la carrera
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length =20)
    private CareerStatus status = CareerStatus.ACTIVE;

    // Semestres
    @Column(nullable = false)
    private Integer semesters = 4;

    // Lista de planes de estudio de la carrera
    @OneToMany(mappedBy = "career", fetch = FetchType.LAZY)
    private List<StudyPlanEntity> studyPlans = new ArrayList<>();

}
