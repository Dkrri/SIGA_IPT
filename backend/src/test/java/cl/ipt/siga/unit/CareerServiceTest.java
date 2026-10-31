package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.career_dto.CareerRequest;
import cl.ipt.siga.dtos.career_dto.CareerResponse;
import cl.ipt.siga.entities.CareerEntity;
import cl.ipt.siga.enums.CareerStatus;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.CareerMapper;
import cl.ipt.siga.repositories.CareerRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.repositories.StudyPlanRepository;
import cl.ipt.siga.services.CareerService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerServiceTest {

    @Mock
    private CareerRepository careerRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CareerMapper mapper;
    @InjectMocks
    private CareerService service;

    private CareerRequest request(String code) {
        CareerRequest request = new CareerRequest();
        request.setCode(code);
        request.setName("Name");
        request.setDescription("Description");
        return request;
    }

    @Test
    void createSuccessfully() {
        CareerRequest request = request("C1");
        CareerEntity career = TestData.career(1L);
        CareerResponse response = new CareerResponse();
        when(careerRepository.existsByCode("C1")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(career);
        when(careerRepository.save(career)).thenReturn(career);
        when(mapper.toResponse(career)).thenReturn(response);

        assertSame(response, service.create(request));
        assertEquals(4, career.getSemesters());
        assertEquals(CareerStatus.ACTIVE, career.getStatus());
    }

    @Test
    void rejectDuplicateCode() {
        when(careerRepository.existsByCode("C1")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(request("C1")));
    }

    @Test
    void findAll() {
        CareerEntity career = TestData.career(1L);
        CareerResponse response = new CareerResponse();
        when(careerRepository.findAll()).thenReturn(List.of(career));
        when(mapper.toResponse(career)).thenReturn(response);
        assertEquals(List.of(response), service.findAll());
    }

    @Test
    void findByIdSuccessfully() {
        CareerEntity career = TestData.career(1L);
        CareerResponse response = new CareerResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(mapper.toResponse(career)).thenReturn(response);
        assertSame(response, service.findById(1L));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(careerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void updateWithSameCode() {
        CareerEntity career = TestData.career(1L);
        CareerRequest request = request("C1");
        CareerResponse response = new CareerResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(careerRepository.save(career)).thenReturn(career);
        when(mapper.toResponse(career)).thenReturn(response);

        assertSame(response, service.update(1L, request));
        verify(careerRepository, org.mockito.Mockito.never()).existsByCode("C1");
    }

    @Test
    void updateWithNewUniqueCode() {
        CareerEntity career = TestData.career(1L);
        CareerRequest request = request("C2");
        CareerResponse response = new CareerResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(careerRepository.existsByCode("C2")).thenReturn(false);
        when(careerRepository.save(career)).thenReturn(career);
        when(mapper.toResponse(career)).thenReturn(response);

        assertSame(response, service.update(1L, request));
        assertEquals("C2", career.getCode());
    }

    @Test
    void updateWithDuplicateCode() {
        CareerEntity career = TestData.career(1L);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(careerRepository.existsByCode("C2")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, request("C2")));
    }

    @Test
    void updateThrowsWhenMissing() {
        when(careerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, request("C1")));
    }

    @Test
    void changeStatus() {
        CareerEntity career = TestData.career(1L);
        CareerResponse response = new CareerResponse();
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(careerRepository.save(career)).thenReturn(career);
        when(mapper.toResponse(career)).thenReturn(response);

        assertSame(response, service.changeStatus(1L, CareerStatus.INACTIVE));
        assertEquals(CareerStatus.INACTIVE, career.getStatus());
    }

    @Test
    void deleteSuccessfully() {
        CareerEntity career = TestData.career(1L);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.existsByCareer(career)).thenReturn(false);
        when(studentRepository.existsByCareer(career)).thenReturn(false);

        service.delete(1L);
        verify(careerRepository).delete(career);
    }

    @Test
    void rejectDeleteWithStudyPlans() {
        CareerEntity career = TestData.career(1L);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.existsByCareer(career)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }

    @Test
    void rejectDeleteWithStudents() {
        CareerEntity career = TestData.career(1L);
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.existsByCareer(career)).thenReturn(false);
        when(studentRepository.existsByCareer(career)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }
}
