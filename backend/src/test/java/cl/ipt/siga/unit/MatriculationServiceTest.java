package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.matriculation_dto.MatriculationRequest;
import cl.ipt.siga.dtos.matriculation_dto.MatriculationResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.entities.MatriculationEntity;
import cl.ipt.siga.entities.StudentEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.MatriculationMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.repositories.MatriculationRepository;
import cl.ipt.siga.repositories.StudentRepository;
import cl.ipt.siga.services.MatriculationService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatriculationServiceTest {

    @Mock
    private MatriculationRepository matriculationRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private MatriculationMapper mapper;
    @InjectMocks
    private MatriculationService service;

    private MatriculationRequest request() {
        MatriculationRequest request = new MatriculationRequest();
        request.setStudentId(1L);
        request.setAcademicPeriodId(2L);
        return request;
    }

    @Test
    void createSuccessfully() {
        StudentEntity student = TestData.student(1L, "s@test.cl", TestData.career(1L), TestData.plan(1L, TestData.career(1L)));
        AcademicPeriodEntity period = TestData.period(2L, AcademicPeriodStatus.OPEN);
        MatriculationEntity entity = new MatriculationEntity();
        MatriculationResponse response = new MatriculationResponse();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(matriculationRepository.existsByStudentAndAcademicPeriod(student, period)).thenReturn(false);
        when(mapper.toEntity(student, period)).thenReturn(entity);
        when(matriculationRepository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.create(request()));
    }

    @Test
    void rejectDuplicateMatriculation() {
        StudentEntity student = TestData.student(1L, "s@test.cl", TestData.career(1L), TestData.plan(1L, TestData.career(1L)));
        AcademicPeriodEntity period = TestData.period(2L, AcademicPeriodStatus.OPEN);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(matriculationRepository.existsByStudentAndAcademicPeriod(student, period)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void createThrowsWhenStudentMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
    }

    @Test
    void createThrowsWhenPeriodMissing() {
        StudentEntity student = TestData.student(1L, "s@test.cl", TestData.career(1L), TestData.plan(1L, TestData.career(1L)));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
    }

    @Test
    void findByStudent() {
        StudentEntity student = TestData.student(1L, "s@test.cl", TestData.career(1L), TestData.plan(1L, TestData.career(1L)));
        MatriculationEntity entity = new MatriculationEntity();
        MatriculationResponse response = new MatriculationResponse();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(matriculationRepository.findByStudent(student)).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertEquals(List.of(response), service.findByStudent(1L));
    }

    @Test
    void findByStudentThrowsWhenMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findByStudent(1L));
    }

    @Test
    void findByPeriod() {
        AcademicPeriodEntity period = TestData.period(2L, AcademicPeriodStatus.OPEN);
        MatriculationEntity entity = new MatriculationEntity();
        MatriculationResponse response = new MatriculationResponse();
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(matriculationRepository.findByAcademicPeriod(period)).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertEquals(List.of(response), service.findByPeriod(2L));
    }

    @Test
    void findByPeriodThrowsWhenMissing() {
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findByPeriod(2L));
    }
}
