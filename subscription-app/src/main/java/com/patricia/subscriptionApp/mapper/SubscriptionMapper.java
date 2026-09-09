package com.patricia.subscriptionApp.mapper;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.dto.SubscriptionDto;

public class SubscriptionMapper {

    private SubscriptionMapper() {}

    public static Subscription toEntity(SubscriptionDto dto) {
        if (dto == null) return null;

        Subscription entity = new Subscription();
        entity.setId(dto.getId());
        entity.setPlano(dto.getPlano());
        entity.setStatus(dto.getStatus());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataExpiracao(dto.getDataExpiracao());

        return entity;
    }

    public static SubscriptionDto toDto(Subscription entity) {
        if (entity == null) return null;

        SubscriptionDto dto = SubscriptionDto.builder()
                .id(entity.getId())
                .plano(entity.getPlano())
                .status(entity.getStatus())
                .dataInicio(entity.getDataInicio())
                .dataExpiracao(entity.getDataExpiracao())
                        .build();

        dto.setUserId(entity.getUser() != null
                ? entity.getUser().getId() : null);

        return dto;
    }

    public static void updateEntity(Subscription entity, SubscriptionDto dto) {
        if (entity == null || dto == null) return;

        entity.setPlano(dto.getPlano());
        entity.setStatus(dto.getStatus());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataExpiracao(dto.getDataExpiracao());

    }
}
