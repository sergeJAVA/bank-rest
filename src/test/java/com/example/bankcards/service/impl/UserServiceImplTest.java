package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameTakenException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static Role role;
    private static String fullName;
    private static String username;
    private static String password;
    private static User user;

    @BeforeAll
    static void setUp() {
        role = new Role(1, "USER");
        fullName = "Test";
        username = "username";
        password = "encodedPass";
        user = User.builder()
                .id(1L)
                .fullName(fullName)
                .username(username)
                .password(password)
                .roles(Set.of(role))
                .build();
    }

    @Test
    @DisplayName("createUser: Успешное создание пользователя")
    void createUser_Success() {

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        UserDto userDto = userService.createUser(fullName, username, password);

        assertNotNull(userDto);
        assertEquals(fullName, userDto.getFullName());
        assertEquals(role.getName(), userDto.getRoles().stream().findFirst().get().getName());

        verify(roleRepository, times(1)).findByName("USER");
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createUser: Ошибка при дублировании имени пользователя")
    void createUser_Failure_DuplicateUsername() {

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Exception exception = assertThrows(UsernameTakenException.class, () -> {
            userService.createUser(fullName, username, password);
        });
        assertEquals("Username <<" + username + ">> is already taken!", exception.getMessage());
    }

    @Test
    @DisplayName("deleteUser: Успешное удаление пользователя")
    void deleteUser_Success() {
        doNothing().when(userRepository).deleteById(user.getId());

        userService.deleteUser(user.getId());

        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    @DisplayName("findById: Успешный поиск пользователя по ID")
    void findById_Success() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto userDto = userService.findById(user.getId());

        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getUsername(), userDto.getUsername());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("findById: Ошибка, когда пользователь не найден")
    void findById_Failure_UserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.findById(userId);
        });

        assertEquals("The user with id <<" + userId + ">> not found!", exception.getMessage());
    }

    @Test
    @DisplayName("findByUsername: Успешный поиск пользователя по имени")
    void findByUsername_Success_UserFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByUsername(username);

        assertTrue(foundUser.isPresent());
        assertEquals(username, foundUser.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername: Пользователь не найден")
    void findByUsername_Failure_UserNotFound() {
        String nonExistentUsername = "nouser";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findByUsername(nonExistentUsername);

        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("findAll: Успешное получение страницы с пользователями")
    void findAll_Success() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserDto> userDtoPage = userService.findAll(0, 10);

        assertNotNull(userDtoPage);
        assertEquals(1, userDtoPage.getTotalElements());
        assertEquals(user.getUsername(), userDtoPage.getContent().get(0).getUsername());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

}