package cl.ipt.siga.config;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.MatriculationEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.enums.StudyPlanStatus;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.enums.Weekday;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.MatriculationRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import cl.ipt.siga.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the database with the study plans from Appendix A of the assignment,
 * plus one teacher, one student, an open academic period, sections for the
 * first-semester subjects and the student's matriculation.
 *
 * <p>The seed is idempotent: it only runs when the database has no careers.
 * It can be disabled with {@code siga.seed.enabled=false}.
 *
 * <p>Emails {@code teacher@siga.local} and {@code student@siga.local} match the
 * Keycloak demo users, so the {@code /me} and {@code /my} endpoints work.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "siga.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final SectionRepository sectionRepository;
    private final MatriculationRepository matriculationRepository;

    @Override
    public void run(String... args) {
        if (careerRepository.count() > 0) {
            log.info("Seed skipped: the database already contains data.");
            return;
        }

        CareerEntity tap = createCareer("10450", "Tecnico Analista Programador",
                "Two-year technical program focused on software development.");
        CareerEntity tad = createCareer("10451", "Tecnico en Analisis de Datos",
                "Two-year technical program focused on data analysis.");
        CareerEntity tcs = createCareer("10452", "Tecnico en Ciberseguridad",
                "Two-year technical program focused on cybersecurity.");

        StudyPlanEntity tapPlan = createPlan("2021.3", 2021, tap);
        StudyPlanEntity tadPlan = createPlan("2022.3", 2022, tad);
        StudyPlanEntity tcsPlan = createPlan("2021.3", 2021, tcs);

        Map<String, SubjectEntity> tapSubjects = seedSubjects(tapPlan, tapSeeds());
        seedSubjects(tadPlan, tadSeeds());
        seedSubjects(tcsPlan, tcsSeeds());

        TeacherEntity teacher = createTeacher();
        StudentEntity student = createStudent(tap, tapPlan);

        AcademicPeriodEntity period = createPeriod("2027-1", 2027, 1);
        seedSections(period, teacher, tapSubjects);
        createMatriculation(student, period);

        log.info("Seed completed: 3 careers, 3 study plans, 72 subjects, "
                + "1 teacher, 1 student, period {} and its sections.", period.getCode());
    }

    // ------------------------------------------------------------------
    // Careers and study plans
    // ------------------------------------------------------------------

    private CareerEntity createCareer(String code, String name, String description) {
        CareerEntity career = new CareerEntity();
        career.setCode(code);
        career.setName(name);
        career.setDescription(description);
        career.setStatus(CareerStatus.ACTIVE);
        career.setSemesters(4);
        return careerRepository.save(career);
    }

    private StudyPlanEntity createPlan(String code, int year, CareerEntity career) {
        StudyPlanEntity plan = new StudyPlanEntity();
        plan.setCode(code);
        plan.setYear(year);
        plan.setStatus(StudyPlanStatus.ACTIVE);
        plan.setCareer(career);
        return studyPlanRepository.save(plan);
    }

    // ------------------------------------------------------------------
    // Subjects
    // ------------------------------------------------------------------

    private record SubjectSeed(String code, String name, int semester,
                               int theory, int exercise, int lab, int sct,
                               List<String> prerequisites) {
    }

    private SubjectSeed subject(String code, String name, int semester,
                                int theory, int exercise, int lab, int sct,
                                String... prerequisites) {
        return new SubjectSeed(code, name, semester, theory, exercise, lab, sct,
                List.of(prerequisites));
    }

    private Map<String, SubjectEntity> seedSubjects(StudyPlanEntity plan, List<SubjectSeed> seeds) {
        Map<String, SubjectEntity> byCode = new HashMap<>();

        for (SubjectSeed seed : seeds) {
            SubjectEntity subject = new SubjectEntity();
            subject.setCode(seed.code());
            subject.setName(seed.name());
            subject.setSemester(seed.semester());
            subject.setTheoryHours(seed.theory());
            subject.setExerciseHours(seed.exercise());
            subject.setLabHours(seed.lab());
            subject.setSct(seed.sct());
            subject.setStudyPlan(plan);
            byCode.put(seed.code(), subjectRepository.save(subject));
        }

        // Second pass: prerequisites (all subjects of the plan already exist)
        for (SubjectSeed seed : seeds) {
            SubjectEntity subject = byCode.get(seed.code());
            for (String prerequisiteCode : seed.prerequisites()) {
                subject.getPrerequisites().add(byCode.get(prerequisiteCode));
            }
            subjectRepository.save(subject);
        }

        return byCode;
    }

    private List<SubjectSeed> tapSeeds() {
        return List.of(
                subject("TAP101", "Fundamentos de Programacion", 1, 2, 2, 2, 6),
                subject("TAP102", "Matematica Aplicada", 1, 2, 2, 0, 5),
                subject("TAP103", "Fundamentos de Computacion", 1, 2, 0, 2, 4),
                subject("TAP104", "Bases de Datos I", 1, 2, 0, 2, 5),
                subject("TAP105", "Comunicacion Efectiva", 1, 2, 2, 0, 4),
                subject("TAP106", "Ingles Tecnico I", 1, 2, 2, 0, 4),
                subject("TAP201", "Programacion Orientada a Objetos", 2, 2, 2, 2, 6, "TAP101"),
                subject("TAP202", "Estructuras de Datos", 2, 2, 2, 2, 6, "TAP101"),
                subject("TAP203", "Bases de Datos II", 2, 2, 0, 2, 5, "TAP104"),
                subject("TAP204", "Desarrollo Web I", 2, 2, 0, 4, 6, "TAP101"),
                subject("TAP205", "Sistemas Operativos", 2, 2, 0, 2, 4, "TAP103"),
                subject("TAP206", "Ingles Tecnico II", 2, 2, 2, 0, 3, "TAP106"),
                subject("TAP301", "Desarrollo Web II", 3, 2, 0, 4, 6, "TAP201", "TAP204"),
                subject("TAP302", "Ingenieria de Software", 3, 2, 2, 0, 5, "TAP201"),
                subject("TAP303", "Desarrollo de Aplicaciones Moviles", 3, 2, 0, 4, 6, "TAP201"),
                subject("TAP304", "Administracion de Bases de Datos", 3, 2, 0, 2, 5, "TAP203"),
                subject("TAP305", "Redes y Comunicaciones", 3, 2, 0, 2, 4, "TAP205"),
                subject("TAP306", "Taller de Programacion", 3, 0, 0, 4, 4, "TAP201", "TAP202"),
                subject("TAP401", "Arquitectura y Desarrollo de Software", 4, 2, 0, 4, 6, "TAP301", "TAP302"),
                subject("TAP402", "Desarrollo de Aplicaciones Cloud", 4, 2, 0, 4, 6, "TAP301"),
                subject("TAP403", "Seguridad de Aplicaciones", 4, 2, 0, 2, 4, "TAP301", "TAP305"),
                subject("TAP404", "Calidad y Pruebas de Software", 4, 2, 0, 2, 5, "TAP302"),
                subject("TAP405", "Proyecto de Desarrollo de Software", 4, 0, 0, 6, 6, "TAP301", "TAP302", "TAP306"),
                subject("TAP406", "Emprendimiento y Gestion de Proyectos", 4, 2, 2, 0, 3, "TAP302")
        );
    }

    private List<SubjectSeed> tadSeeds() {
        return List.of(
                subject("TAD101", "Fundamentos de Programacion", 1, 2, 2, 2, 6),
                subject("TAD102", "Matematica Aplicada", 1, 2, 2, 0, 5),
                subject("TAD103", "Introduccion al Analisis de Datos", 1, 2, 0, 2, 5),
                subject("TAD104", "Bases de Datos I", 1, 2, 0, 2, 5),
                subject("TAD105", "Herramientas Computacionales", 1, 0, 0, 4, 4),
                subject("TAD106", "Comunicacion Efectiva", 1, 2, 2, 0, 4),
                subject("TAD201", "Programacion para Analisis de Datos", 2, 2, 0, 4, 6, "TAD101"),
                subject("TAD202", "Probabilidad y Estadistica", 2, 2, 2, 2, 6, "TAD102"),
                subject("TAD203", "Bases de Datos II", 2, 2, 0, 2, 5, "TAD104"),
                subject("TAD204", "Preparacion y Calidad de Datos", 2, 2, 0, 4, 5, "TAD103", "TAD101"),
                subject("TAD205", "Visualizacion de Datos", 2, 2, 0, 2, 5, "TAD103"),
                subject("TAD206", "Ingles Tecnico", 2, 2, 2, 0, 3),
                subject("TAD301", "Estadistica Aplicada", 3, 2, 2, 2, 6, "TAD202"),
                subject("TAD302", "Mineria de Datos", 3, 2, 0, 4, 6, "TAD201", "TAD202"),
                subject("TAD303", "Inteligencia de Negocios", 3, 2, 0, 4, 5, "TAD203", "TAD205"),
                subject("TAD304", "Integracion y Procesamiento de Datos", 3, 2, 0, 4, 5, "TAD201", "TAD203"),
                subject("TAD305", "Visualizacion Avanzada de Datos", 3, 2, 0, 2, 4, "TAD205"),
                subject("TAD306", "Etica y Proteccion de Datos", 3, 2, 2, 0, 4, "TAD103"),
                subject("TAD401", "Fundamentos de Machine Learning", 4, 2, 0, 4, 6, "TAD301", "TAD302"),
                subject("TAD402", "Big Data y Procesamiento de Datos", 4, 2, 0, 4, 5, "TAD304"),
                subject("TAD403", "Analitica de Negocios", 4, 2, 0, 2, 5, "TAD301", "TAD303"),
                subject("TAD404", "Desarrollo de Soluciones de Datos", 4, 2, 0, 4, 5, "TAD302", "TAD304"),
                subject("TAD405", "Proyecto de Analisis de Datos", 4, 0, 0, 6, 6, "TAD302", "TAD303", "TAD305"),
                subject("TAD406", "Emprendimiento y Gestion de Proyectos", 4, 2, 2, 0, 3)
        );
    }

    private List<SubjectSeed> tcsSeeds() {
        return List.of(
                subject("TCS101", "Fundamentos de Programacion", 1, 2, 2, 2, 6),
                subject("TCS102", "Fundamentos de Sistemas Computacionales", 1, 2, 0, 2, 5),
                subject("TCS103", "Redes de Computadores I", 1, 2, 0, 2, 5),
                subject("TCS104", "Sistemas Operativos", 1, 2, 0, 2, 5),
                subject("TCS105", "Matematica Aplicada", 1, 2, 2, 0, 5),
                subject("TCS106", "Comunicacion Efectiva", 1, 2, 2, 0, 4),
                subject("TCS201", "Programacion para Ciberseguridad", 2, 2, 0, 4, 5, "TCS101"),
                subject("TCS202", "Redes de Computadores II", 2, 2, 0, 4, 6, "TCS103"),
                subject("TCS203", "Administracion de Sistemas Linux", 2, 2, 0, 4, 5, "TCS104"),
                subject("TCS204", "Fundamentos de Ciberseguridad", 2, 2, 2, 2, 6, "TCS102", "TCS103"),
                subject("TCS205", "Bases de Datos", 2, 2, 0, 2, 5, "TCS101"),
                subject("TCS206", "Ingles Tecnico", 2, 2, 2, 0, 3),
                subject("TCS301", "Seguridad de Redes", 3, 2, 0, 4, 6, "TCS202", "TCS204"),
                subject("TCS302", "Seguridad de Sistemas Operativos", 3, 2, 0, 4, 5, "TCS203", "TCS204"),
                subject("TCS303", "Criptografia Aplicada", 3, 2, 2, 2, 5, "TCS204"),
                subject("TCS304", "Seguridad de Aplicaciones Web", 3, 2, 0, 4, 5, "TCS201", "TCS204"),
                subject("TCS305", "Monitoreo y Deteccion de Amenazas", 3, 2, 0, 4, 5, "TCS202", "TCS204"),
                subject("TCS306", "Legislacion y Etica en Ciberseguridad", 3, 2, 2, 0, 4, "TCS204"),
                subject("TCS401", "Ethical Hacking y Pruebas de Penetracion", 4, 2, 0, 4, 6, "TCS301", "TCS302"),
                subject("TCS402", "Gestion de Incidentes de Ciberseguridad", 4, 2, 0, 4, 5, "TCS305"),
                subject("TCS403", "Seguridad en Cloud", 4, 2, 0, 4, 5, "TCS301", "TCS302"),
                subject("TCS404", "Analisis Forense Digital", 4, 2, 0, 4, 5, "TCS302"),
                subject("TCS405", "Proyecto de Ciberseguridad", 4, 0, 0, 6, 6, "TCS301", "TCS302", "TCS305"),
                subject("TCS406", "Gestion de Riesgos de Ciberseguridad", 4, 2, 2, 0, 3, "TCS204")
        );
    }

    // ------------------------------------------------------------------
    // Demo people and academic period
    // ------------------------------------------------------------------

    private TeacherEntity createTeacher() {
        TeacherEntity teacher = new TeacherEntity();
        teacher.setRun("11111111-1");
        teacher.setFirstName("Docente");
        teacher.setLastName("Demo");
        teacher.setEmail("teacher@siga.local");
        teacher.setProfessionalTitle("Ingeniero Civil en Informatica");
        teacher.setAcademicDegree("Magister en Ingenieria de Software");
        teacher.setStatus(TeacherStatus.ACTIVE);
        return teacherRepository.save(teacher);
    }

    private StudentEntity createStudent(CareerEntity career, StudyPlanEntity plan) {
        StudentEntity student = new StudentEntity();
        student.setRun("22222222-2");
        student.setFirstName("Estudiante");
        student.setLastName("Demo");
        student.setEmail("student@siga.local");
        student.setCareer(career);
        student.setStudyPlan(plan);
        student.setStatus(StudentStatus.REGULAR);
        return studentRepository.save(student);
    }

    private AcademicPeriodEntity createPeriod(String code, int year, int semester) {
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setCode(code);
        period.setYear(year);
        period.setSemester(semester);
        period.setStatus(AcademicPeriodStatus.OPEN);
        return academicPeriodRepository.save(period);
    }

    // ------------------------------------------------------------------
    // Sections and matriculation
    // ------------------------------------------------------------------

    private void seedSections(AcademicPeriodEntity period,
                               TeacherEntity teacher,
                               Map<String, SubjectEntity> subjects) {

        // All first-semester subjects of the TAP plan.
        List<SubjectEntity> firstSemester = subjects.values().stream()
                .filter(subject -> subject.getSemester() == 1)
                .sorted(Comparator.comparing(SubjectEntity::getCode))
                .toList();

        // All available weekday + module slots.
        List<Slot> slots = new ArrayList<>();
        for (Weekday weekday : Weekday.values()) {
            for (int module = 1; module <= 6; module++) {
                slots.add(new Slot(weekday, module));
            }
        }

        int slotIndex = 0;
        for (SubjectEntity subject : firstSemester) {
            int telTotal = subject.getTheoryHours()
                    + subject.getExerciseHours()
                    + subject.getLabHours();
            int requiredBlocks = telTotal / 2;

            SectionEntity section = new SectionEntity();
            section.setSubject(subject);
            section.setAcademicPeriod(period);
            section.setTeacher(teacher);
            section.setCapacity(60);

            List<ScheduleBlockEntity> blocks = new ArrayList<>();
            for (int i = 0; i < requiredBlocks; i++) {
                Slot slot = slots.get(slotIndex++);
                ScheduleBlockEntity block = new ScheduleBlockEntity();
                block.setWeekday(slot.weekday());
                block.setModule(slot.module());
                block.setSection(section);
                blocks.add(block);
            }
            section.setScheduleBlocks(blocks);
            sectionRepository.save(section);
        }
    }

    private record Slot(Weekday weekday, int module) {
    }

    private void createMatriculation(StudentEntity student, AcademicPeriodEntity period) {
        MatriculationEntity matriculation = new MatriculationEntity();
        matriculation.setStudent(student);
        matriculation.setAcademicPeriod(period);
        matriculation.setRegisteredAt(LocalDate.now());
        matriculationRepository.save(matriculation);
    }
}
