package subscriptionApp.controller;

import com.patricia.subscriptionApp.controller.UserController;
import com.patricia.subscriptionApp.dto.PageResponse;
import com.patricia.subscriptionApp.dto.UserDto;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    void getAll_shouldReturnUsers() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        when(service.findAll()).thenReturn(List.of(User.builder().id(UUID.randomUUID()).email("a@b.com").build()));
        ResponseEntity<List<UserDto>> resp = controller.getAll();
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void getById_shouldReturnDto() throws Exception {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        UUID id = UUID.randomUUID();
        User u = User.builder().id(id).email("a@b.com").build();
        when(service.findById(id)).thenReturn(u);
        ResponseEntity<UserDto> resp = controller.getById(id);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(id, resp.getBody().getId());
    }

    @Test
    void getAllPaginated_shouldReturnPageResponse() throws Exception {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        UUID id = UUID.randomUUID();
        User u = User.builder().id(id).email("a@b.com").build();
        Page<User> page = new PageImpl<>(List.of(u));
        when(service.findAllPaginated(0, 10, "id", "ASC")).thenReturn(page);
        ResponseEntity<PageResponse<UserDto>> resp = controller.getAllPaginated(0, 10, "id", "ASC");
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().getContent().size());
        assertEquals(id, resp.getBody().getContent().get(0).getId());
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        User created = User.builder().id(UUID.randomUUID()).email("c@d.com").build();
        when(service.create(any())).thenReturn(created);
        ResponseEntity<UserDto> resp = controller.create(UserDto.builder().email("c@d.com").build());
        assertEquals(201, resp.getStatusCode().value());
        assertEquals(created.getId(), resp.getBody().getId());
    }

    @Test
    void update_shouldDelegateAndReturnOk() throws Exception {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        UUID id = UUID.randomUUID();
        UserDto dto = UserDto.builder().id(id).email("z@y.com").build();
        User updated = User.builder().id(id).email("z@y.com").build();
        when(service.update(id, dto)).thenReturn(updated);
        ResponseEntity<UserDto> resp = controller.update(id, dto);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(id, resp.getBody().getId());
        verify(service).update(id, dto);
    }

    @Test
    void exists_shouldReturn200WhenExists() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        UUID id = UUID.randomUUID();
        when(service.existsById(id)).thenReturn(true);
        ResponseEntity<Void> resp = controller.exists(id);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void exists_shouldReturn404WhenNotExists() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        UUID id = UUID.randomUUID();
        when(service.existsById(id)).thenReturn(false);
        ResponseEntity<Void> resp = controller.exists(id);
        assertEquals(404, resp.getStatusCode().value());
    }
}
