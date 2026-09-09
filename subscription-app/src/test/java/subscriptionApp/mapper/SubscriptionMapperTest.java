package subscriptionApp.mapper;

import com.patricia.subscriptionApp.dto.SubscriptionDto;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.mapper.SubscriptionMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionMapperTest {

    @Test
    void toDto_shouldCopyFieldsAndExtractUserId() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().id(uid).email("a@b.com").build();
        Subscription entity = Subscription.builder()
                .id(UUID.randomUUID())
                .user(user)
                .plano(PlanType.BASICO)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(7))
                .build();

        SubscriptionDto dto = SubscriptionMapper.toDto(entity);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(uid, dto.getUserId());
        assertEquals(entity.getPlano(), dto.getPlano());
    }

    @Test
    void toEntity_and_updateEntity_shouldCopyFields() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .id(UUID.randomUUID())
                .plano(PlanType.BASICO)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(7))
                .build();

        Subscription entity = SubscriptionMapper.toEntity(dto);
        assertEquals(dto.getId(), entity.getId());

        SubscriptionMapper.updateEntity(entity, dto);
        assertEquals(dto.getPlano(), entity.getPlano());
        assertEquals(dto.getStatus(), entity.getStatus());
    }
}

