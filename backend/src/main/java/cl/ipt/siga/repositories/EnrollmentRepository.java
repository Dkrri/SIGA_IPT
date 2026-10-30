package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.EnrollmentEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    /** Epic 1: a student with enrollments cannot be deleted or change career/study plan. */
    boolean existsByStudent(StudentEntity student);

    /** Epic 3: a subject with enrollments is not deleted. */
    boolean existsBySubject(SubjectEntity subject);

    /** Epic 5: a section with enrolled students is not deleted. */
    boolean existsBySection(SectionEntity section);

    // --- Epic 6 ---

    List<EnrollmentEntity> findByStudentAndAcademicPeriod(StudentEntity student,
                                                          AcademicPeriodEntity period);

    long countByStudentAndAcademicPeriod(StudentEntity student,
                                         AcademicPeriodEntity period);

    boolean existsByStudentAndSubjectAndAcademicPeriod(StudentEntity student,
                                                       SubjectEntity subject,
                                                       AcademicPeriodEntity period);

    long countBySection(SectionEntity section);

    List<EnrollmentEntity> findBySection(SectionEntity section);

    // --- Epic 7 ---

    List<EnrollmentEntity> findByAcademicPeriod(AcademicPeriodEntity period);

    /** Period enrollments that still have no final grade (closing). */
    List<EnrollmentEntity> findByAcademicPeriodAndFinalGradeIsNull(AcademicPeriodEntity period);
}
