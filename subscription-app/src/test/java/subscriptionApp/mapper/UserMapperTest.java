package subscriptionApp.mapper;

import com.patricia.subscriptionApp.dto.UserDto;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toDto_and_toEntity_shouldCopyFields() {
        User user = User.builder().id(UUID.randomUUID()).email("a@b.com").build();
        UserDto dto = UserMapper.toDto(user);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());

        User entity = UserMapper.toEntity(dto);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getEmail(), entity.getEmail());
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        User entity = User.builder().id(UUID.randomUUID()).email("old@b.com").build();
        UserDto dto = UserDto.builder().email("new@b.com").build();
        UserMapper.updateEntity(entity, dto);
        assertEquals("new@b.com", entity.getEmail());
    }
}

