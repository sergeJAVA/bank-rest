package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserService {

    UserDto createUser(String fullName, String username, String password);

    void deleteUser(Long userId);

    UserDto findById(Long id);

    Optional<User> findByUsername(String username);

    Page<UserDto> findAll(int page, int size);

}
