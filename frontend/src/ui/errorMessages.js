/**
 * Translates backend error messages to Spanish for the UI.
 * The backend keeps its messages in English (assignment requirement);
 * this layer only changes what the user sees.
 */

const FIELD_LABELS = {
  year: 'Año',
  semester: 'Semestre',
  capacity: 'Cupos',
  finalGrade: 'Nota',
  code: 'Código',
  name: 'Nombre',
  run: 'RUN',
  email: 'Correo',
  careerId: 'Carrera',
  studyPlanId: 'Plan de estudio',
  subjectId: 'Asignatura',
  teacherId: 'Docente',
  academicPeriodId: 'Período académico',
  studentId: 'Estudiante',
  sectionIds: 'Secciones',
  scheduleBlocks: 'Bloques de horario',
  theoryHours: 'Horas de teoría',
  exerciseHours: 'Horas de ejercicios',
  labHours: 'Horas de laboratorio',
  sct: 'SCT',
  professionalTitle: 'Título profesional',
  academicDegree: 'Grado académico',
  prerequisites: 'Prerrequisitos',
  temporaryPassword: 'Contraseña temporal',
};

const EXACT = {
  'Validation failed': 'La validación de los datos falló.',
  'The operation violates a database constraint': 'La operación viola una restricción de la base de datos.',
  'Malformed request body': 'El cuerpo de la solicitud no es válido.',
  'Network Error': 'No se pudo conectar con el servidor.',
  'Unexpected error': 'Ocurrió un error inesperado.',

  'You must select at least one section': 'Debes seleccionar al menos una sección.',
  'All sections must belong to the same period': 'Todas las secciones deben pertenecer al mismo período.',
  'The student is not in REGULAR status': 'El estudiante no está en estado Regular.',
  'You cannot enroll in the same subject twice': 'No puedes inscribir la misma asignatura dos veces.',
  'You can only cancel your own enrollments': 'Solo puedes anular tus propias inscripciones.',
  'Semester must be between 1 and 4': 'El semestre debe estar entre 1 y 4.',
  'SCT must be greater than 0 and less than or equal to 7': 'El SCT debe ser mayor que 0 y menor o igual a 7.',
  'TEL hours cannot be negative': 'Las horas TEL no pueden ser negativas.',
  'The sum of TEL hours must be less than 8': 'La suma de horas TEL debe ser menor que 8.',
  'A subject can have between 0 and 3 prerequisites': 'Una asignatura puede tener entre 0 y 3 prerrequisitos.',
  'A subject cannot be a prerequisite of itself': 'Una asignatura no puede ser prerrequisito de sí misma.',
  'A subject cannot change its study plan': 'Una asignatura no puede cambiar de plan de estudio.',
  'Cannot delete: the subject is part of the academic history': 'No se puede eliminar: la asignatura forma parte del historial académico.',
  'Cannot delete: the subject has enrollments': 'No se puede eliminar: la asignatura tiene inscripciones.',
  'Cannot delete: the subject has associated sections': 'No se puede eliminar: la asignatura tiene secciones asociadas.',
  'Cannot delete: the teacher has associated sections': 'No se puede eliminar: el docente tiene secciones asociadas.',
  'Cannot delete: the section has enrolled students': 'No se puede eliminar: la sección tiene estudiantes inscritos.',
  'Cannot delete the study plan: it has associated students': 'No se puede eliminar el plan: tiene estudiantes asociados.',
  'Cannot delete: the career has associated study plans. Only its status can be changed to INACTIVE.':
    'No se puede eliminar: la carrera tiene planes de estudio asociados. Solo puede cambiar su estado a Inactiva.',
  'Cannot delete: the career has associated students. Only its status can be changed to INACTIVE.':
    'No se puede eliminar: la carrera tiene estudiantes asociados. Solo puede cambiar su estado a Inactiva.',
  'Cannot deactivate: the teacher has sections in an OPEN period':
    'No se puede desactivar: el docente tiene secciones en un período abierto.',
  'A student with that RUN already exists': 'Ya existe un estudiante con ese RUN.',
  'A student with that email already exists': 'Ya existe un estudiante con ese correo.',
  'The career is not active and does not accept students': 'La carrera no está activa y no acepta estudiantes.',
  'The student has enrollments or academic history; it cannot be deleted or change career/study plan':
    'El estudiante tiene inscripciones o historial académico; no se puede eliminar ni cambiar de carrera o plan.',
  'A teacher with that RUN already exists': 'Ya existe un docente con ese RUN.',
  'A teacher with that email already exists': 'Ya existe un docente con ese correo.',
  'Only ACTIVE teachers can be assigned to sections': 'Solo se pueden asignar docentes activos a las secciones.',
  'Capacity must be between 1 and 60': 'Los cupos deben estar entre 1 y 60.',
  'The section must have at least one schedule block': 'La sección debe tener al menos un bloque de horario.',
  'Total TEL hours must be even to form 2-hour blocks': 'El TEL total debe ser par para formar bloques de 2 horas.',
  'Grade must be between 1.0 and 7.0': 'La nota debe estar entre 1.0 y 7.0.',
  'Only the teacher responsible for the section can register grades':
    'Solo el docente responsable de la sección puede registrar notas.',

  'RUN is required': 'El RUN es obligatorio.',
  'First name is required': 'El nombre es obligatorio.',
  'Last name is required': 'El apellido es obligatorio.',
  'Email is required': 'El correo es obligatorio.',
  'Email format is invalid': 'El formato del correo no es válido.',
  'Career is required': 'La carrera es obligatoria.',
  'Code is required': 'El código es obligatorio.',
  'Study plan code is required': 'El código del plan de estudio es obligatorio.',
  'Year is required': 'El año es obligatorio.',
  'Semester is required': 'El semestre es obligatorio.',
  'Semester must be 1 or 2': 'El semestre debe ser 1 o 2.',
  'Year is invalid': 'El año no es válido.',
  'Subject is required': 'La asignatura es obligatoria.',
  'Academic period is required': 'El período académico es obligatorio.',
  'Teacher is required': 'El docente es obligatorio.',
  'Capacity is required': 'Los cupos son obligatorios.',
  'Module is required': 'El módulo es obligatorio.',
  'Weekday is required': 'El día es obligatorio.',
  'Grade is required': 'La nota es obligatoria.',
  'Enrollment is required': 'La inscripción es obligatoria.',
  'SCT is required': 'El SCT es obligatorio.',
  'Student is required': 'El estudiante es obligatorio.',
  'Professional title is required': 'El título profesional es obligatorio.',
  'Academic degree is required': 'El grado académico es obligatorio.',
  'Temporary password is required': 'La contraseña temporal es obligatoria.',
  'Temporary password must be between 8 and 72 characters':
    'La contraseña temporal debe tener entre 8 y 72 caracteres.',
  'RUN must have the format 1111111-1': 'Debe tener el formato 1111111-1.',
};

const PATTERNS = [
  [/^Academic period (.+) is not OPEN$/, (m) => `El período académico ${m[1]} no está abierto.`],
  [/^The student is not matriculated in period (.+)$/, (m) => `El estudiante no está matriculado en el período ${m[1]}.`],
  [/^Subject (.+) does not belong to the student's study plan$/, (m) => `La asignatura ${m[1]} no pertenece al plan de estudio del estudiante.`],
  [/^Subject (.+) is already approved$/, (m) => `La asignatura ${m[1]} ya está aprobada.`],
  [/^Subject (.+) is already enrolled in the period$/, (m) => `La asignatura ${m[1]} ya está inscrita en el período.`],
  [/^Prerequisite not approved: (.+) to enroll in (.+)$/, (m) => `No has aprobado el prerrequisito ${m[1]} para inscribir ${m[2]}.`],
  [/^The section for subject (.+) has no available seats$/, (m) => `La sección de ${m[1]} no tiene cupos disponibles.`],
  [/^Schedule conflict in block (.+)$/, (m) => `Choque de horario en el bloque ${m[1]}.`],
  [/^You cannot have more than (\d+) subjects enrolled in the period$/, (m) => `No puedes tener más de ${m[1]} asignaturas inscritas en el período.`],
  [/^You must enroll in at least (\d+) subjects in the period$/, (m) => `Debes inscribir al menos ${m[1]} asignaturas en el período.`],
  [/^You cannot enroll in (.+): you have pending subjects from lower semesters$/, (m) => `No puedes inscribir ${m[1]}: tienes asignaturas pendientes de semestres inferiores.`],
  [/^Enrollment not found: (\d+)$/, (m) => `Inscripción no encontrada: ${m[1]}.`],
  [/^Enrollment (\d+) does not belong to section (\d+)$/, (m) => `La inscripción ${m[1]} no pertenece a la sección ${m[2]}.`],
  [/^Cannot close: there are pending grades in: (.+)$/, (m) => `No se puede cerrar: hay notas pendientes en: ${m[1]}.`],
  [/^Only an OPEN period can be closed \(current status: (.+)\)$/, (m) => `Solo se puede cerrar un período abierto (estado actual: ${m[1]}).`],
  [/^A career already exists with code (.+)$/, (m) => `Ya existe una carrera con el código ${m[1]}.`],
  [/^A study plan already exists with code (.+) in career (.+)$/, (m) => `Ya existe un plan de estudio con el código ${m[1]} en la carrera ${m[2]}.`],
  [/^Career (.+) has no current study plan$/, (m) => `La carrera ${m[1]} no tiene un plan de estudio vigente.`],
  [/^A subject already exists with code (.+) in plan (.+)$/, (m) => `Ya existe una asignatura con el código ${m[1]} en el plan ${m[2]}.`],
  [/^Another subject already exists with code (.+) in plan (.+)$/, (m) => `Ya existe otra asignatura con el código ${m[1]} en el plan ${m[2]}.`],
  [/^Prerequisite (.+) does not belong to the same study plan$/, (m) => `El prerrequisito ${m[1]} no pertenece al mismo plan de estudio.`],
  [/^Prerequisite (.+) must be from a previous semester$/, (m) => `El prerrequisito ${m[1]} debe ser de un semestre anterior.`],
  [/^Status (.+) cannot be assigned manually$/, (m) => `El estado ${m[1]} no se puede asignar manualmente.`],
  [/^A section already exists for subject (.+) in period (.+)$/, (m) => `Ya existe una sección para la asignatura ${m[1]} en el período ${m[2]}.`],
  [/^Another section already exists for subject (.+) in period (.+)$/, (m) => `Ya existe otra sección para la asignatura ${m[1]} en el período ${m[2]}.`],
  [/^Duplicate schedule block: (.+)$/, (m) => `Bloque de horario duplicado: ${m[1]}.`],
  [/^The section must have (\d+) blocks \(total TEL \/ 2\)$/, (m) => `La sección debe tener ${m[1]} bloques (TEL total / 2).`],
  [/^The teacher already has a section in block (.+)$/, (m) => `El docente ya tiene una sección en el bloque ${m[1]}.`],
  [/^The student is already matriculated in period (.+)$/, (m) => `El estudiante ya está matriculado en el período ${m[1]}.`],
  [/^An academic period already exists for (.+)$/, (m) => `Ya existe un período académico para ${m[1]}.`],
  [/^Invalid value for parameter: (.+)$/, (m) => `Valor inválido para el parámetro: ${m[1]}.`],
  [/^A Keycloak user already exists with email (.+)$/, (m) => `Ya existe un usuario de Keycloak con el correo ${m[1]}.`],
  [/^Could not provision the Keycloak user: (.+)$/, (m) => `No se pudo crear el usuario en Keycloak: ${m[1]}.`],
  [/^Could not authenticate with the Keycloak Admin API: (.+)$/, (m) => `No se pudo autenticar con la API de administración de Keycloak: ${m[1]}.`],
  [/^Keycloak did not return (.+)$/, (m) => `Keycloak no devolvió ${m[1]}.`],
  [/^(.+) cannot exceed (\d+) characters$/, (m) => `${m[1]} no puede superar ${m[2]} caracteres.`],
];

function translatePart(part) {
  const trimmed = part.trim();
  if (!trimmed) return trimmed;

  const fieldMatch = trimmed.match(/^([A-Za-z0-9_]+):\s*(.*)$/);
  if (fieldMatch) {
    const label = FIELD_LABELS[fieldMatch[1]] ?? fieldMatch[1];
    return `${label}: ${translatePart(fieldMatch[2])}`;
  }

  if (EXACT[trimmed]) return EXACT[trimmed];

  for (const [pattern, replacement] of PATTERNS) {
    const match = trimmed.match(pattern);
    if (match) {
      return replacement(match);
    }
  }

  return trimmed;
}

/** Translates one backend message (or a "; "-separated validation list). */
export function translateErrorMessage(message) {
  if (!message) return 'Ocurrió un error inesperado.';
  return message
    .split('; ')
    .map(translatePart)
    .join(' ');
}
