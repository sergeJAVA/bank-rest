package com.example.bankcards.data;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInit {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    void init() {
        Optional<Role> role = roleRepository.findByName("ADMIN");

        Optional<User> user = userRepository.findByUsername("ADMIN");

        if (user.isPresent()) {
            return;
        }

        if (role.isPresent()) {
            Role rl = role.get();
            Set<Role> roles = new HashSet<>();
            roles.add(rl);
            User admin = User.builder()
                    .username("ADMIN")
                    .fullName("ADMIN")
                    .password(passwordEncoder.encode("password123"))
                    .roles(roles)
                    .build();
            userRepository.save(admin);
        }
    }
}
