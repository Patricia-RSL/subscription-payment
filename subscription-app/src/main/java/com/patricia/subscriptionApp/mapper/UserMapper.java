package com.patricia.subscriptionApp.mapper;

import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.dto.UserDto;

public class UserMapper {

    private UserMapper() {}

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;

        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto toDto(User entity) {
        if (entity == null) return null;

        return UserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .build();
    }

    public static void updateEntity(User entity, UserDto dto) {
        if (entity == null || dto == null) return;

        entity.setEmail(dto.getEmail());

    }
}
