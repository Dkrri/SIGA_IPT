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
import cl.ipt.siga.enums.Weekday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 6 integration tests for grade registration:
 * 3 success scenarios and 3 rejection scenarios.
 */
class GradeIT extends IntegrationTestSupport {

    private StudentEntity firstStudent;
    private StudentEntity secondStudent;
    private TeacherEntity owner;
    private TeacherEntity otherTeacher;
    private AcademicPeriodEntity period;
    private SectionEntity section;
    private EnrollmentEntity firstEnrollment;
    private EnrollmentEntity secondEnrollment;

    @BeforeEach
    void setUp() {
        CareerEntity career = career("T-GRADE");
        StudyPlanEntity plan = plan(career, "PLAN-GRADE");
        SubjectEntity subject = subject(plan, "GRD101", 1, 2, 0, 0);
        period = period("2026-2", 2026, 2, AcademicPeriodStatus.OPEN);
        owner = teacher("33333333-3", "owner.grade@test.cl");
        otherTeacher = teacher("44444444-4", "other.grade@test.cl");
        section = section(period, subject, owner, 10, Weekday.WEDNESDAY, 1);

        firstStudent = student("55555555-5", "first.grade@test.cl", career, plan);
        secondStudent = student("66666666-6", "second.grade@test.cl", career, plan);
        firstEnrollment = enrollment(firstStudent, section, null, EnrollmentResult.PENDING);
        secondEnrollment = enrollment(secondStudent, section, null, EnrollmentResult.PENDING);
    }

    @Test
    void registerApprovedGradeSuccessfully() throws Exception {
        mockMvc.perform(put("/api/enrollments/{id}/grade", firstEnrollment.getId())
                        .with(as("TEACHER", owner.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalGrade\":5.5}"))
                .andExpect(status().isOk());

        EnrollmentEntity saved = enrollmentRepository.findById(firstEnrollment.getId()).orElseThrow();
        assertEquals(5.5, saved.getFinalGrade());
        assertEquals(EnrollmentResult.APPROVED, saved.getResult());
    }

    @Test
    void registerFailedGradeSuccessfully() throws Exception {
        mockMvc.perform(put("/api/enrollments/{id}/grade", firstEnrollment.getId())
                        .with(as("TEACHER", owner.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalGrade\":3.9}"))
                .andExpect(status().isOk());

        EnrollmentEntity saved = enrollmentRepository.findById(firstEnrollment.getId()).orElseThrow();
        assertEquals(3.9, saved.getFinalGrade());
        assertEquals(EnrollmentResult.FAILED, saved.getResult());
    }

    @Test
    void registerBulkGradesSuccessfully() throws Exception {
        String body = "{\"grades\":["
                + "{\"enrollmentId\":" + firstEnrollment.getId() + ",\"finalGrade\":6.0},"
                + "{\"enrollmentId\":" + secondEnrollment.getId() + ",\"finalGrade\":4.0}"
                + "]}";

        mockMvc.perform(put("/api/sections/{id}/grades", section.getId())
                        .with(as("TEACHER", owner.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        assertEquals(6.0, enrollmentRepository.findById(firstEnrollment.getId()).orElseThrow().getFinalGrade());
        assertEquals(4.0, enrollmentRepository.findById(secondEnrollment.getId()).orElseThrow().getFinalGrade());
    }

    @Test
    void rejectGradeOutOfRange() throws Exception {
        mockMvc.perform(put("/api/enrollments/{id}/grade", firstEnrollment.getId())
                        .with(as("TEACHER", owner.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalGrade\":8.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectGradeFromNonOwnerTeacher() throws Exception {
        mockMvc.perform(put("/api/enrollments/{id}/grade", firstEnrollment.getId())
                        .with(as("TEACHER", otherTeacher.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalGrade\":5.0}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectGradeWhenPeriodIsClosed() throws Exception {
        period.setStatus(AcademicPeriodStatus.CLOSED);
        academicPeriodRepository.save(period);

        mockMvc.perform(put("/api/enrollments/{id}/grade", firstEnrollment.getId())
                        .with(as("TEACHER", owner.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalGrade\":5.0}"))
                .andExpect(status().isConflict());
    }
}
