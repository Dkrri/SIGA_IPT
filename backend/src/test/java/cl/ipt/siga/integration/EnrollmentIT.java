package cl.ipt.siga.integration;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.enums.Weekday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 6 integration tests for the enrollment process:
 * 3 success scenarios and 3 rejection scenarios.
 */
class EnrollmentIT extends IntegrationTestSupport {

    private StudentEntity student;
    private TeacherEntity teacher;
    private AcademicPeriodEntity period;
    private final List<SectionEntity> sections = new ArrayList<>();

    @BeforeEach
    void setUp() {
        CareerEntity career = career("T-ENR");
        StudyPlanEntity plan = plan(career, "PLAN-ENR");
        teacher = teacher("11111111-1", "teacher.enr@test.cl");
        student = student("22222222-2", "student.enr@test.cl", career, plan);
        period = period("2026-1", 2026, 1, AcademicPeriodStatus.OPEN);
        matriculation(student, period);

        Weekday[] days = {Weekday.MONDAY, Weekday.MONDAY, Weekday.MONDAY,
                Weekday.TUESDAY, Weekday.TUESDAY, Weekday.TUESDAY};
        for (int i = 0; i < 6; i++) {
            SubjectEntity subject = subject(plan, "ENR" + (i + 1), 1, 2, 0, 0);
            sections.add(section(period, subject, teacher, 30, days[i], i + 1));
        }
    }

    private List<Long> ids(int... indexes) {
        List<Long> result = new ArrayList<>();
        for (int index : indexes) {
            result.add(sections.get(index).getId());
        }
        return result;
    }

    private String sectionsBody(int... indexes) {
        return "{\"sectionIds\":" + ids(indexes) + "}";
    }

    @Test
    void enrollThreeSubjectsSuccessfully() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .with(as("STUDENT", student.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sectionsBody(0, 1, 2)))
                .andExpect(status().isCreated());

        assertEquals(3, enrollmentRepository.count());
    }

    @Test
    void enrollSixSubjectsSuccessfully() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .with(as("STUDENT", student.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sectionsBody(0, 1, 2, 3, 4, 5)))
                .andExpect(status().isCreated());

        assertEquals(6, enrollmentRepository.count());
    }

    @Test
    void cancelOwnEnrollmentSuccessfully() throws Exception {
        var enrollment = enrollment(student, sections.get(0), null, EnrollmentResult.PENDING);

        mockMvc.perform(delete("/api/enrollments/{id}", enrollment.getId())
                        .with(as("STUDENT", student.getEmail())))
                .andExpect(status().isNoContent());

        assertEquals(0, enrollmentRepository.count());
    }

    @Test
    void rejectFewerThanThreeSubjects() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .with(as("STUDENT", student.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sectionsBody(0, 1)))
                .andExpect(status().isConflict());

        assertEquals(0, enrollmentRepository.count());
    }

    @Test
    void rejectSectionWithoutCapacity() throws Exception {
        sections.get(0).setCapacity(0);
        sectionRepository.save(sections.get(0));

        mockMvc.perform(post("/api/enrollments")
                        .with(as("STUDENT", student.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sectionsBody(0, 1, 2)))
                .andExpect(status().isConflict());

        assertEquals(0, enrollmentRepository.count());
    }

    @Test
    void rejectScheduleConflict() throws Exception {
        SectionEntity second = sections.get(1);
        second.getScheduleBlocks().get(0).setWeekday(Weekday.MONDAY);
        second.getScheduleBlocks().get(0).setModule(1);
        sectionRepository.save(second);

        mockMvc.perform(post("/api/enrollments")
                        .with(as("STUDENT", student.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sectionsBody(0, 1, 2)))
                .andExpect(status().isConflict());

        assertEquals(0, enrollmentRepository.count());
    }
}
