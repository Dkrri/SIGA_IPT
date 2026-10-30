package cl.ipt.siga.mappers;

import cl.ipt.siga.dtos.section_dto.ScheduleBlockRequest;
import cl.ipt.siga.dtos.section_dto.ScheduleBlockResponse;
import cl.ipt.siga.dtos.section_dto.SectionRequest;
import cl.ipt.siga.dtos.section_dto.SectionResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.ScheduleBlockEntity;
import cl.ipt.siga.entities.SectionEntity;
import cl.ipt.siga.entities.SubjectEntity;
import cl.ipt.siga.entities.TeacherEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class SectionMapper {

    /**
     * DTO -> entity.
     * Subject, period and teacher are resolved by the service.
     * Blocks are built here and linked on both sides of the relation.
     */
    public SectionEntity toEntity(SectionRequest request,
                                  SubjectEntity subject,
                                  AcademicPeriodEntity academicPeriod,
                                  TeacherEntity teacher) {

        SectionEntity section = new SectionEntity();
        section.setSubject(subject);
        section.setAcademicPeriod(academicPeriod);
        section.setTeacher(teacher);
        section.setCapacity(request.getCapacity());

        for (ScheduleBlockRequest blockRequest : request.getScheduleBlocks()) {
            ScheduleBlockEntity block = new ScheduleBlockEntity();
            block.setWeekday(blockRequest.getWeekday());
            block.setModule(blockRequest.getModule());
            block.setSection(section);
            section.getScheduleBlocks().add(block);
        }

        return section;
    }

    /**
     * Entity -> output DTO.
     * Must be called inside a transaction: it accesses Lazy relations.
     */
    public SectionResponse toResponse(SectionEntity section) {
        SectionResponse response = new SectionResponse();

        response.setId(section.getId());
        response.setCapacity(section.getCapacity());

        if (section.getSubject() != null) {
            response.setSubjectId(section.getSubject().getId());
            response.setSubjectCode(section.getSubject().getCode());
            response.setSubjectName(section.getSubject().getName());
        }

        if (section.getAcademicPeriod() != null) {
            response.setAcademicPeriodId(section.getAcademicPeriod().getId());
            response.setAcademicPeriodCode(section.getAcademicPeriod().getCode());
        }

        if (section.getTeacher() != null) {
            response.setTeacherId(section.getTeacher().getId());
            response.setTeacherName(
                    section.getTeacher().getFirstName() + " " + section.getTeacher().getLastName());
        }

        if (section.getScheduleBlocks() != null) {
            response.setScheduleBlocks(section.getScheduleBlocks().stream()
                    .sorted(Comparator.comparing(ScheduleBlockEntity::getWeekday)
                            .thenComparing(ScheduleBlockEntity::getModule))
                    .map(this::toBlockResponse)
                    .toList());
        }

        return response;
    }

    public ScheduleBlockResponse toBlockResponse(ScheduleBlockEntity block) {
        return new ScheduleBlockResponse(
                block.getWeekday(),
                block.getModule(),
                block.getWeekday().getCode() + block.getModule()
        );
    }
}
