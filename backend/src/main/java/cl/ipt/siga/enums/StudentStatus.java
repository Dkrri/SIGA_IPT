package cl.ipt.siga.enums;

public enum StudentStatus {
    REGULAR, // estudiante habilitado académicamente para continuar sus estudios.
    POSTPONEMENT, // estudiante que ha postergado sus estudios.
    TEMPORARY_WITHDRAWAL, // estudiante que ha interrumpido temporalmente sus estudios.
    GRADUATED, // estudiante que ha aprobado todas las asignaturas correspondientes a su plan de estudios.
    ELIMINATED // estudiante que ha perdido su condición de estudiante de la carrera por haber reprobado una misma asignatura por segunda vez.
}
