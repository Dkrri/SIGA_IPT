package cl.ipt.siga.repositories;

import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {

    /** Epic 3: a subject with sections is not deleted. */
    boolean existsBySubject(SubjectEntity subject);

    /** Epic 4: a teacher with sections is not deleted. */
    boolean existsByTeacher(TeacherEntity teacher);

    /** Epic 4: do not deactivate a teacher with sections in an OPEN period. */
    boolean existsByTeacherAndAcademicPeriodStatus(TeacherEntity teacher, AcademicPeriodStatus status);

    /** Epic 5: a subject can have only one section per period. */
    boolean existsBySubjectAndAcademicPeriod(SubjectEntity subject, AcademicPeriodEntity period);

    /** Epic 5: same as above, excluding the section itself (update). */
    boolean existsBySubjectAndAcademicPeriodAndIdNot(SubjectEntity subject,
                                                     AcademicPeriodEntity period,
                                                     Long id);

    /** Epic 5: sections of a teacher in a period (for schedule conflicts). */
    List<SectionEntity> findByTeacherAndAcademicPeriod(TeacherEntity teacher,
                                                       AcademicPeriodEntity period);

    /** Epic 6: the offered section of a subject in a period. */
    Optional<SectionEntity> findBySubjectAndAcademicPeriod(SubjectEntity subject,
                                                           AcademicPeriodEntity period);

    List<SectionEntity> findByAcademicPeriod(AcademicPeriodEntity period);

    List<SectionEntity> findByTeacher(TeacherEntity teacher);
}
