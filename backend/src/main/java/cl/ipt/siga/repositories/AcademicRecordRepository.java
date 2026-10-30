package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.AcademicRecordEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.enums.EnrollmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecordEntity, Long> {

    /** Epic 1: a student with academic history cannot be deleted or change career/study plan. */
    boolean existsByStudent(StudentEntity student);

    /** Epic 3: a subject in the academic history is not deleted. */
    boolean existsBySubject(SubjectEntity subject);

    /** Epic 6: tells whether a subject is approved or failed. */
    boolean existsByStudentAndSubjectAndResult(StudentEntity student,
                                               SubjectEntity subject,
                                               EnrollmentResult result);

    List<AcademicRecordEntity> findByStudent(StudentEntity student);
}
