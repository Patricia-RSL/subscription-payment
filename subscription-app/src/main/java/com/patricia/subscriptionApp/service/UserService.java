package com.patricia.subscriptionApp.service;

import com.patricia.subscriptionApp.dto.UserDto;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> findAll();

    Page<User> findAllPaginated(int page, int size, String sortBy, String sortDirection) throws BadRequestException;

    User findById(UUID id) throws BadRequestException;

    @Transactional
    User create(UserDto dto) throws BadRequestException;

    @Transactional
    User update(UUID id, UserDto dto) throws BadRequestException;

    boolean existsById(UUID id);
}
