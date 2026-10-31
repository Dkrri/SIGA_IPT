/**
 * Spanish labels for values coming from the backend enums.
 * The code keeps the original English enum values; only the UI is translated.
 */
export const ROLE_LABELS = {
  ADMIN: 'Administrador',
  TEACHER: 'Docente',
  STUDENT: 'Estudiante',
};

export const CAREER_STATUS_LABELS = {
  ACTIVE: 'Activa',
  INACTIVE: 'Inactiva',
};

export const STUDY_PLAN_STATUS_LABELS = {
  ACTIVE: 'Vigente',
  INACTIVE: 'No vigente',
};

export const TEACHER_STATUS_LABELS = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
};

export const ACADEMIC_PERIOD_STATUS_LABELS = {
  OPEN: 'Abierto',
  CLOSED: 'Cerrado',
};

export const STUDENT_STATUS_LABELS = {
  REGULAR: 'Regular',
  POSTPONEMENT: 'Postergación',
  TEMPORARY_WITHDRAWAL: 'Retiro temporal',
  GRADUATED: 'Graduado',
  ELIMINATED: 'Eliminado',
};

export const ENROLLMENT_RESULT_LABELS = {
  PENDING: 'Pendiente',
  APPROVED: 'Aprobado',
  FAILED: 'Reprobado',
};

export const WEEKDAY_LABELS = {
  MONDAY: 'Lunes',
  TUESDAY: 'Martes',
  WEDNESDAY: 'Miércoles',
  THURSDAY: 'Jueves',
  FRIDAY: 'Viernes',
};

export const labelFor = (labels, value) => labels[value] ?? value;
