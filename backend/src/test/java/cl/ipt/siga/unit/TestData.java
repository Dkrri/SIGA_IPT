package cl.ipt.siga.unit;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.AcademicRecordEntity;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
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

import java.time.LocalDateTime;

/** Small entity builders shared by the unit tests. */
public final class TestData {

    private TestData() {
    }

    public static CareerEntity career(Long id) {
        CareerEntity career = new CareerEntity();
        career.setId(id);
        career.setCode("C" + id);
        career.setName("Career " + id);
        career.setDescription("Description " + id);
        career.setSemesters(4);
        career.setStatus(CareerStatus.ACTIVE);
        return career;
    }

    public static StudyPlanEntity plan(Long id, CareerEntity career) {
        StudyPlanEntity plan = new StudyPlanEntity();
        plan.setId(id);
        plan.setCode("P" + id);
        plan.setYear(2026);
        plan.setCareer(career);
        plan.setStatus(StudyPlanStatus.ACTIVE);
        return plan;
    }

    public static SubjectEntity subject(Long id, StudyPlanEntity plan, int semester, int tel) {
        SubjectEntity subject = new SubjectEntity();
        subject.setId(id);
        subject.setCode("S" + id);
        subject.setName("Subject " + id);
        subject.setSemester(semester);
        subject.setTheoryHours(tel);
        subject.setExerciseHours(0);
        subject.setLabHours(0);
        subject.setSct(4);
        subject.setStudyPlan(plan);
        return subject;
    }

    public static TeacherEntity teacher(Long id, String email) {
        TeacherEntity teacher = new TeacherEntity();
        teacher.setId(id);
        teacher.setRun(String.format("%08d-1", id));
        teacher.setFirstName("Teacher");
        teacher.setLastName("" + id);
        teacher.setEmail(email);
        teacher.setProfessionalTitle("Engineer");
        teacher.setAcademicDegree("MSc");
        teacher.setStatus(TeacherStatus.ACTIVE);
        return teacher;
    }

    public static StudentEntity student(Long id, String email, CareerEntity career, StudyPlanEntity plan) {
        StudentEntity student = new StudentEntity();
        student.setId(id);
        student.setRun(String.format("%08d-2", id));
        student.setFirstName("Student");
        student.setLastName("" + id);
        student.setEmail(email);
        student.setCareer(career);
        student.setStudyPlan(plan);
        student.setStatus(StudentStatus.REGULAR);
        return student;
    }

    public static AcademicPeriodEntity period(Long id, AcademicPeriodStatus status) {
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setId(id);
        period.setCode("2026-" + id);
        period.setYear(2026);
        period.setSemester(1);
        period.setStatus(status);
        return period;
    }

    public static SectionEntity section(Long id,
                                        SubjectEntity subject,
                                        AcademicPeriodEntity period,
                                        TeacherEntity teacher,
                                        int capacity) {
        SectionEntity section = new SectionEntity();
        section.setId(id);
        section.setSubject(subject);
        section.setAcademicPeriod(period);
        section.setTeacher(teacher);
        section.setCapacity(capacity);

        ScheduleBlockEntity block = new ScheduleBlockEntity();
        block.setId(id);
        block.setWeekday(Weekday.MONDAY);
        block.setModule(1);
        block.setSection(section);
        section.getScheduleBlocks().add(block);
        return section;
    }

    public static EnrollmentEntity enrollment(Long id,
                                              StudentEntity student,
                                              SectionEntity section,
                                              Double finalGrade,
                                              EnrollmentResult result) {
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setId(id);
        enrollment.setStudent(student);
        enrollment.setSubject(section.getSubject());
        enrollment.setSection(section);
        enrollment.setAcademicPeriod(section.getAcademicPeriod());
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setFinalGrade(finalGrade);
        enrollment.setResult(result);
        return enrollment;
    }

    public static AcademicRecordEntity record(Long id,
                                              StudentEntity student,
                                              SubjectEntity subject,
                                              AcademicPeriodEntity period,
                                              double grade,
                                              EnrollmentResult result) {
        AcademicRecordEntity record = new AcademicRecordEntity();
        record.setId(id);
        record.setStudent(student);
        record.setSubject(subject);
        record.setAcademicPeriod(period);
        record.setFinalGrade(grade);
        record.setResult(result);
        record.setConsolidatedAt(LocalDateTime.now());
        return record;
    }
}
