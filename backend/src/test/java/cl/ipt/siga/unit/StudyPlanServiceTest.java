package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.studyplan_dto.StudyPlanRequest;
import cl.ipt.siga.dtos.studyplan_dto.StudyPlanResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.entities.StudyPlanEntity;
import cl.ipt.siga.enums.StudyPlanStatus;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.StudyPlanMapper;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.services.StudyPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private CareerRepository careerRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudyPlanMapper mapper;
    @InjectMocks
    private StudyPlanService service;

    private StudyPlanRequest request(Long careerId, boolean current) {
        StudyPlanRequest request = new StudyPlanRequest();
        request.setCode("P1");
        request.setYear(2026);
        request.setCareerId(careerId);
        request.setCurrent(current);
        return request;
    }

    @Test
    void createCurrentPlanAndDeactivatePrevious() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity current = TestData.plan(9L, career);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudyPlanResponse response = new StudyPlanResponse();

        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.existsByCareerAndCode(career, "P1")).thenReturn(false);
        when(mapper.toEntity(any(StudyPlanRequest.class), eq(career))).thenReturn(plan);
        when(studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.of(current));
        when(studyPlanRepository.save(plan)).thenReturn(plan);
        when(mapper.toResponse(plan)).thenReturn(response);

        assertSame(response, service.create(request(1L, true)));
        assertEquals(StudyPlanStatus.INACTIVE, current.getStatus());
        assertEquals(StudyPlanStatus.ACTIVE, plan.getStatus());
    }

    @Test
    void createNonCurrentPlan() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudyPlanResponse response = new StudyPlanResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.existsByCareerAndCode(career, "P1")).thenReturn(false);
        when(mapper.toEntity(any(StudyPlanRequest.class), eq(career))).thenReturn(plan);
        when(studyPlanRepository.save(plan)).thenReturn(plan);
        when(mapper.toResponse(plan)).thenReturn(response);

        assertSame(response, service.create(request(1L, false)));
        assertEquals(StudyPlanStatus.INACTIVE, plan.getStatus());
    }

    @Test
    void rejectDuplicatePlanCode() {
        CareerEntity career = TestData.career(1L);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.existsByCareerAndCode(career, "P1")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request(1L, true)));
    }

    @Test
    void createThrowsWhenCareerMissing() {
        when(careerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request(1L, true)));
    }

    @Test
    void findByCareer() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudyPlanResponse response = new StudyPlanResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerOrderByYearDesc(career)).thenReturn(List.of(plan));
        when(mapper.toResponse(plan)).thenReturn(response);

        assertEquals(List.of(response), service.findByCareer(1L));
    }

    @Test
    void findByCareerThrowsWhenMissing() {
        when(careerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findByCareer(1L));
    }

    @Test
    void findCurrentSuccessfully() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudyPlanResponse response = new StudyPlanResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan));
        when(mapper.toResponse(plan)).thenReturn(response);

        assertSame(response, service.findCurrent(1L));
    }

    @Test
    void findCurrentThrowsWhenMissing() {
        CareerEntity career = TestData.career(1L);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.findCurrent(1L));
    }

    @Test
    void setCurrentSuccessfully() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        StudyPlanResponse response = new StudyPlanResponse();
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(studyPlanRepository.findByCareerAndStatus(career, StudyPlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan));
        when(studyPlanRepository.save(plan)).thenReturn(plan);
        when(mapper.toResponse(plan)).thenReturn(response);

        assertSame(response, service.setCurrent(1L));
        assertEquals(StudyPlanStatus.ACTIVE, plan.getStatus());
    }

    @Test
    void setCurrentThrowsWhenMissing() {
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.setCurrent(1L));
    }

    @Test
    void deleteSuccessfully() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(studentRepository.existsByStudyPlan(plan)).thenReturn(false);

        service.delete(1L);
        verify(studyPlanRepository).delete(plan);
    }

    @Test
    void rejectDeleteWithStudents() {
        CareerEntity career = TestData.career(1L);
        StudyPlanEntity plan = TestData.plan(1L, career);
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(studentRepository.existsByStudyPlan(plan)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }
}
