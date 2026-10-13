package cl.ipt.siga.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Es una clase abstracta, reúne los datos personales comunes de estudiantes y docentes
 * NO es una tabla propia
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserProfileEntity {

    // RUT/RUN
    @Column(nullable = false, unique = true, length = 20)
    private String run;

    // Nombre
    @Column(nullable = false, length = 100)
    private String firstName;

    // Apellido
    @Column(nullable = false, length = 100)
    private String lastName;

    // Correo
    @Column(nullable = false, unique = true, length = 150)
    private String email;
}
