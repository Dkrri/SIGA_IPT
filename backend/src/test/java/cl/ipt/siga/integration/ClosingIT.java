package cl.ipt.siga.integration;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.EnrollmentResult;
import cl.ipt.siga.enums.StudentStatus;
import cl.ipt.siga.enums.Weekday;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 6 integration tests for academic period closing:
 * 3 success scenarios and 3 rejection scenarios.
 */
class ClosingIT extends IntegrationTestSupport {

    private record Scenario(CareerEntity career,
                            StudyPlanEntity plan,
                            SubjectEntity subject,
                            TeacherEntity teacher,
                            StudentEntity student,
                            AcademicPeriodEntity period,
                            SectionEntity section) {
    }

    private Scenario scenario(String suffix, int subjectCount) {
        CareerEntity career = career("T-CLOSE-" + suffix);
        StudyPlanEntity plan = plan(career, "PLAN-CLOSE-" + suffix);
        SubjectEntity mainSubject = subject(plan, "CLS-" + suffix, 1, 2, 0, 0);
        for (int i = 1; i < subjectCount; i++) {
            subject(plan, "CLS-" + suffix + "-" + i, 1, 2, 0, 0);
        }
        TeacherEntity teacher = teacher("77777777-" + suffix, "teacher.close." + suffix + "@test.cl");
        StudentEntity student = student(
                "88888888-" + suffix,
                "student.close." + suffix + "@test.cl",
                career,
                plan);
        AcademicPeriodEntity period = period("2026-" + suffix, 2026, 1, AcademicPeriodStatus.OPEN);
        SectionEntity section = section(period, mainSubject, teacher, 10, Weekday.THURSDAY, 1);
        return new Scenario(career, plan, mainSubject, teacher, student, period, section);
    }

    @Test
    void closePeriodAndConsolidateHistory() throws Exception {
        Scenario scenario = scenario("1", 2);
        enrollment(scenario.student(), scenario.section(), 5.0, EnrollmentResult.PENDING);

        mockMvc.perform(post("/api/academic-periods/{id}/close", scenario.period().getId())
                        .with(as("ADMIN", "admin.close@test.cl")))
                .andExpect(status().isOk());

        assertEquals(AcademicPeriodStatus.CLOSED,
                academicPeriodRepository.findById(scenario.period().getId()).orElseThrow().getStatus());
        assertEquals(1, academicRecordRepository.count());
        assertEquals(StudentStatus.REGULAR,
                studentRepository.findById(scenario.student().getId()).orElseThrow().getStatus());
    }

    @Test
    void closePeriodAndGraduateStudent() throws Exception {
        Scenario scenario = scenario("2", 1);
        enrollment(scenario.student(), scenario.section(), 6.0, EnrollmentResult.PENDING);

        mockMvc.perform(post("/api/academic-periods/{id}/close", scenario.period().getId())
                        .with(as("ADMIN", "admin.close@test.cl")))
                .andExpect(status().isOk());

        assertEquals(StudentStatus.GRADUATED,
                studentRepository.findById(scenario.student().getId()).orElseThrow().getStatus());
    }

    @Test
    void closePeriodAndEliminateStudentAfterSecondFailure() throws Exception {
        Scenario scenario = scenario("3", 1);
        AcademicPeriodEntity previous = period("2025-3", 2025, 1, AcademicPeriodStatus.CLOSED);
        academicRecord(scenario.student(), scenario.subject(), previous, 3.0, EnrollmentResult.FAILED);
        enrollment(scenario.student(), scenario.section(), 3.9, EnrollmentResult.PENDING);

        mockMvc.perform(post("/api/academic-periods/{id}/close", scenario.period().getId())
                        .with(as("ADMIN", "admin.close@test.cl")))
                .andExpect(status().isOk());

        assertEquals(StudentStatus.ELIMINATED,
                studentRepository.findById(scenario.student().getId()).orElseThrow().getStatus());
    }

    @Test
    void rejectCloseWithPendingGrades() throws Exception {
        Scenario scenario = scenario("4", 1);
        enrollment(scenario.student(), scenario.section(), null, EnrollmentResult.PENDING);

        mockMvc.perform(post("/api/academic-periods/{id}/close", scenario.period().getId())
                        .with(as("ADMIN", "admin.close@test.cl")))
                .andExpect(status().isConflict());

        assertEquals(AcademicPeriodStatus.OPEN,
                academicPeriodRepository.findById(scenario.period().getId()).orElseThrow().getStatus());
    }

    @Test
    void rejectCloseWhenPeriodIsAlreadyClosed() throws Exception {
        AcademicPeriodEntity closed = period("2024-4", 2024, 2, AcademicPeriodStatus.CLOSED);

        mockMvc.perform(post("/api/academic-periods/{id}/close", closed.getId())
                        .with(as("ADMIN", "admin.close@test.cl")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectCloseWhenPeriodDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/academic-periods/{id}/close", 999999)
                        .with(as("ADMIN", "admin.close@test.cl")))
                .andExpect(status().isNotFound());
    }
}
