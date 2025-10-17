package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.mapper.UserMapper;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameTakenException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDto createUser(String fullName, String username, String password) {
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role with name <<USER>> not found!"));

        if (existUserByUsername(username)) {
            throw new UsernameTakenException("Username <<" + username + ">> is already taken!");
        }

        User user = User.builder()
                .fullName(fullName)
                .username(username)
                .password(password)
                .roles(Set.of(role))
                .build();
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("The user with id <<" + id + ">> not found!"));
        return UserMapper.toDto(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Page<UserDto> findAll(int page, int size) {
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        return users.map(UserMapper::toDto);
    }

    private boolean existUserByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}
