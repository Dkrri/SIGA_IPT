package cl.ipt.siga.unit;

import cl.ipt.siga.dtos.teacher_dto.TeacherRequest;
import cl.ipt.siga.dtos.teacher_dto.TeacherResponse;
import cl.ipt.siga.dtos.teacher_dto.TeacherUpdateRequest;
import cl.ipt.siga.entities.TeacherEntity;
import cl.ipt.siga.enums.AcademicPeriodStatus;
import cl.ipt.siga.enums.TeacherStatus;
import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.exceptions.ResourceNotFoundException;
import cl.ipt.siga.mappers.TeacherMapper;
import cl.ipt.siga.repositories.SectionRepository;
import cl.ipt.siga.repositories.TeacherRepository;
import cl.ipt.siga.services.KeycloakAdminService;
import cl.ipt.siga.services.TeacherService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private TeacherMapper mapper;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @InjectMocks
    private TeacherService service;

    private TeacherRequest request() {
        TeacherRequest request = new TeacherRequest();
        request.setRun("11111111-1");
        request.setFirstName("Name");
        request.setLastName("Last");
        request.setEmail("t@test.cl");
        request.setProfessionalTitle("Engineer");
        request.setAcademicDegree("MSc");
        request.setTemporaryPassword("Temp12345");
        return request;
    }

    private TeacherUpdateRequest updateRequest(String email) {
        TeacherUpdateRequest request = new TeacherUpdateRequest();
        request.setFirstName("Name");
        request.setLastName("Last");
        request.setEmail(email);
        request.setProfessionalTitle("Engineer");
        request.setAcademicDegree("MSc");
        return request;
    }

    @Test
    void createSuccessfully() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        TeacherResponse response = new TeacherResponse();
        when(teacherRepository.existsByRun("11111111-1")).thenReturn(false);
        when(teacherRepository.existsByEmail("t@test.cl")).thenReturn(false);
        when(mapper.toEntity(any(TeacherRequest.class))).thenReturn(teacher);
        when(teacherRepository.save(teacher)).thenReturn(teacher);
        when(mapper.toResponse(teacher)).thenReturn(response);

        assertSame(response, service.create(request()));
        verify(keycloakAdminService).createUser("t@test.cl", "Teacher", "1", "Temp12345", "TEACHER");
    }

    @Test
    void rejectDuplicateRun() {
        when(teacherRepository.existsByRun("11111111-1")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void rejectDuplicateEmail() {
        when(teacherRepository.existsByRun("11111111-1")).thenReturn(false);
        when(teacherRepository.existsByEmail("t@test.cl")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(request()));
    }

    @Test
    void updateSuccessfullyWithSameEmail() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        TeacherResponse response = new TeacherResponse();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(teacher)).thenReturn(teacher);
        when(mapper.toResponse(teacher)).thenReturn(response);

        assertSame(response, service.update(1L, updateRequest("t@test.cl")));
    }

    @Test
    void updateSuccessfullyWithNewEmail() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        TeacherResponse response = new TeacherResponse();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByEmail("new@test.cl")).thenReturn(false);
        when(teacherRepository.save(teacher)).thenReturn(teacher);
        when(mapper.toResponse(teacher)).thenReturn(response);

        assertSame(response, service.update(1L, updateRequest("new@test.cl")));
        assertEquals("new@test.cl", teacher.getEmail());
    }

    @Test
    void rejectUpdateWithDuplicateEmail() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByEmail("new@test.cl")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, updateRequest("new@test.cl")));
    }

    @Test
    void changeStatusToActive() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        teacher.setStatus(TeacherStatus.INACTIVE);
        TeacherResponse response = new TeacherResponse();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(teacher)).thenReturn(teacher);
        when(mapper.toResponse(teacher)).thenReturn(response);

        assertSame(response, service.changeStatus(1L, TeacherStatus.ACTIVE));
        assertEquals(TeacherStatus.ACTIVE, teacher.getStatus());
    }

    @Test
    void changeStatusToInactiveWithoutOpenSections() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        TeacherResponse response = new TeacherResponse();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherAndAcademicPeriodStatus(
                teacher, AcademicPeriodStatus.OPEN)).thenReturn(false);
        when(teacherRepository.save(teacher)).thenReturn(teacher);
        when(mapper.toResponse(teacher)).thenReturn(response);

        assertSame(response, service.changeStatus(1L, TeacherStatus.INACTIVE));
    }

    @Test
    void rejectDeactivateWithOpenSections() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherAndAcademicPeriodStatus(
                teacher, AcademicPeriodStatus.OPEN)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.changeStatus(1L, TeacherStatus.INACTIVE));
    }

    @Test
    void deleteSuccessfully() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacher(teacher)).thenReturn(false);

        service.delete(1L);
        verify(teacherRepository).delete(teacher);
    }

    @Test
    void rejectDeleteWithSections() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacher(teacher)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
    }

    @Test
    void readOperations() {
        TeacherEntity teacher = TestData.teacher(1L, "t@test.cl");
        TeacherResponse response = new TeacherResponse();
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByRun("11111111-1")).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByEmail("t@test.cl")).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByStatus(TeacherStatus.ACTIVE)).thenReturn(List.of(teacher));
        when(mapper.toResponse(teacher)).thenReturn(response);

        assertEquals(List.of(response), service.findAll());
        assertSame(response, service.findById(1L));
        assertSame(response, service.findByRun("11111111-1"));
        assertSame(response, service.findByEmail("t@test.cl"));
        assertEquals(List.of(response), service.findByStatus(TeacherStatus.ACTIVE));
    }

    @Test
    void readOperationsThrowWhenMissing() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());
        when(teacherRepository.findByRun("11111111-1")).thenReturn(Optional.empty());
        when(teacherRepository.findByEmail("t@test.cl")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByRun("11111111-1"));
        assertThrows(ResourceNotFoundException.class, () -> service.findByEmail("t@test.cl"));
    }
}
