package cl.ipt.siga.integration;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.AcademicRecordEntity;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.MatriculationEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.enums.StudyPlanStatus;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.enums.Weekday;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.AcademicRecordRepository;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.EnrollmentRepository;
import cl.ipt.siga.repositories.MatriculationRepository;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.repositories.SubjectRepository;
import cl.ipt.siga.repositories.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Base class for integration tests: real Spring context, MockMvc, H2 database,
 * and small fixtures shared by the enrollment, grade and closing tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CareerRepository careerRepository;

    @Autowired
    protected StudyPlanRepository studyPlanRepository;

    @Autowired
    protected SubjectRepository subjectRepository;

    @Autowired
    protected TeacherRepository teacherRepository;

    @Autowired
    protected StudentRepository studentRepository;

    @Autowired
    protected AcademicPeriodRepository academicPeriodRepository;

    @Autowired
    protected SectionRepository sectionRepository;

    @Autowired
    protected EnrollmentRepository enrollmentRepository;

    @Autowired
    protected MatriculationRepository matriculationRepository;

    @Autowired
    protected AcademicRecordRepository academicRecordRepository;

    /** JWT post-processor with the email claim and the application role. */
    protected RequestPostProcessor as(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .claim("email", email)
                        .claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    protected CareerEntity career(String code) {
        CareerEntity career = new CareerEntity();
        career.setCode(code);
        career.setName("Career " + code);
        career.setDescription("Description " + code);
        career.setSemesters(4);
        career.setStatus(CareerStatus.ACTIVE);
        return careerRepository.save(career);
    }

    protected StudyPlanEntity plan(CareerEntity career, String code) {
        StudyPlanEntity plan = new StudyPlanEntity();
        plan.setCode(code);
        plan.setYear(2026);
        plan.setCareer(career);
        plan.setStatus(StudyPlanStatus.ACTIVE);
        return studyPlanRepository.save(plan);
    }

    protected SubjectEntity subject(StudyPlanEntity plan,
                                    String code,
                                    int semester,
                                    int theory,
                                    int exercise,
                                    int lab) {
        SubjectEntity subject = new SubjectEntity();
        subject.setCode(code);
        subject.setName("Subject " + code);
        subject.setSemester(semester);
        subject.setTheoryHours(theory);
        subject.setExerciseHours(exercise);
        subject.setLabHours(lab);
        subject.setSct(4);
        subject.setStudyPlan(plan);
        return subjectRepository.save(subject);
    }

    protected TeacherEntity teacher(String run, String email) {
        TeacherEntity teacher = new TeacherEntity();
        teacher.setRun(run);
        teacher.setFirstName("Teacher");
        teacher.setLastName(run);
        teacher.setEmail(email);
        teacher.setProfessionalTitle("Engineer");
        teacher.setAcademicDegree("MSc");
        teacher.setStatus(TeacherStatus.ACTIVE);
        return teacherRepository.save(teacher);
    }

    protected StudentEntity student(String run,
                                    String email,
                                    CareerEntity career,
                                    StudyPlanEntity plan) {
        StudentEntity student = new StudentEntity();
        student.setRun(run);
        student.setFirstName("Student");
        student.setLastName(run);
        student.setEmail(email);
        student.setCareer(career);
        student.setStudyPlan(plan);
        student.setStatus(StudentStatus.REGULAR);
        return studentRepository.save(student);
    }

    protected AcademicPeriodEntity period(String code,
                                          int year,
                                          int semester,
                                          AcademicPeriodStatus status) {
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setCode(code);
        period.setYear(year);
        period.setSemester(semester);
        period.setStatus(status);
        return academicPeriodRepository.save(period);
    }

    protected SectionEntity section(AcademicPeriodEntity period,
                                     SubjectEntity subject,
                                     TeacherEntity teacher,
                                     int capacity,
                                     Weekday weekday,
                                     int module) {
        SectionEntity section = new SectionEntity();
        section.setAcademicPeriod(period);
        section.setSubject(subject);
        section.setTeacher(teacher);
        section.setCapacity(capacity);

        ScheduleBlockEntity block = new ScheduleBlockEntity();
        block.setWeekday(weekday);
        block.setModule(module);
        block.setSection(section);
        section.getScheduleBlocks().add(block);

        return sectionRepository.save(section);
    }

    protected MatriculationEntity matriculation(StudentEntity student, AcademicPeriodEntity period) {
        MatriculationEntity matriculation = new MatriculationEntity();
        matriculation.setStudent(student);
        matriculation.setAcademicPeriod(period);
        matriculation.setRegisteredAt(LocalDate.now());
        return matriculationRepository.save(matriculation);
    }

    protected EnrollmentEntity enrollment(StudentEntity student,
                                          SectionEntity section,
                                          Double finalGrade,
                                          EnrollmentResult result) {
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setSubject(section.getSubject());
        enrollment.setSection(section);
        enrollment.setAcademicPeriod(section.getAcademicPeriod());
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setFinalGrade(finalGrade);
        enrollment.setResult(result);
        return enrollmentRepository.save(enrollment);
    }

    protected AcademicRecordEntity academicRecord(StudentEntity student,
                                                  SubjectEntity subject,
                                                  AcademicPeriodEntity period,
                                                  double grade,
                                                  EnrollmentResult result) {
        AcademicRecordEntity record = new AcademicRecordEntity();
        record.setStudent(student);
        record.setSubject(subject);
        record.setAcademicPeriod(period);
        record.setFinalGrade(grade);
        record.setResult(result);
        record.setConsolidatedAt(LocalDateTime.now());
        return academicRecordRepository.save(record);
    }
}
