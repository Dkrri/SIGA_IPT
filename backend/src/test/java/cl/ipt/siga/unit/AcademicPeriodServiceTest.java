package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodRequest;
import cl.ipt.siga.dtos.academic_period_dto.AcademicPeriodResponse;
import cl.ipt.siga.entities.AcademicPeriodEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.AcademicPeriodMapper;
import cl.ipt.siga.repositories.AcademicPeriodRepository;
import cl.ipt.siga.services.AcademicPeriodService;
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
class AcademicPeriodServiceTest {

    @Mock
    private AcademicPeriodRepository repository;

    @Mock
    private AcademicPeriodMapper mapper;

    @InjectMocks
    private AcademicPeriodService service;

    private AcademicPeriodRequest request() {
        AcademicPeriodRequest request = new AcademicPeriodRequest();
        request.setYear(2026);
        request.setSemester(1);
        return request;
    }

    @Test
    void createSuccessfully() {
        AcademicPeriodRequest request = request();
        AcademicPeriodEntity period = TestData.period(1L, AcademicPeriodStatus.OPEN);
        AcademicPeriodResponse response = new AcademicPeriodResponse();

        when(repository.existsByYearAndSemester(2026, 1)).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(period);
        when(repository.save(period)).thenReturn(period);
        when(mapper.toResponse(period)).thenReturn(response);

        assertSame(response, service.create(request));
        verify(repository).save(period);
    }

    @Test
    void rejectDuplicatePeriod() {
        when(repository.existsByYearAndSemester(2026, 1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void findAll() {
        AcademicPeriodEntity period = TestData.period(1L, AcademicPeriodStatus.OPEN);
        AcademicPeriodResponse response = new AcademicPeriodResponse();
        when(repository.findAll()).thenReturn(List.of(period));
        when(mapper.toResponse(period)).thenReturn(response);

        assertEquals(List.of(response), service.findAll());
    }

    @Test
    void findByIdSuccessfully() {
        AcademicPeriodEntity period = TestData.period(1L, AcademicPeriodStatus.OPEN);
        AcademicPeriodResponse response = new AcademicPeriodResponse();
        when(repository.findById(1L)).thenReturn(Optional.of(period));
        when(mapper.toResponse(period)).thenReturn(response);

        assertSame(response, service.findById(1L));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findByCodeSuccessfully() {
        AcademicPeriodEntity period = TestData.period(1L, AcademicPeriodStatus.OPEN);
        AcademicPeriodResponse response = new AcademicPeriodResponse();
        when(repository.findByCode("2026-1")).thenReturn(Optional.of(period));
        when(mapper.toResponse(period)).thenReturn(response);

        assertSame(response, service.findByCode("2026-1"));
    }

    @Test
    void findByCodeThrowsWhenMissing() {
        when(repository.findByCode("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByCode("missing"));
    }

    @Test
    void findByStatus() {
        AcademicPeriodEntity period = TestData.period(1L, AcademicPeriodStatus.OPEN);
        AcademicPeriodResponse response = new AcademicPeriodResponse();
        when(repository.findByStatus(AcademicPeriodStatus.OPEN)).thenReturn(List.of(period));
        when(mapper.toResponse(period)).thenReturn(response);

        assertEquals(List.of(response), service.findByStatus(AcademicPeriodStatus.OPEN));
    }
}
