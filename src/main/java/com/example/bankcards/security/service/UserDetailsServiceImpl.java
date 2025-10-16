package com.example.bankcards.security.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new CustomUserDetails(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getRoles().stream()
                                .map(Role::getName)
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toSet())
                )).orElseThrow(() -> new UsernameNotFoundException("The user with username <<" + username + ">> not found!"));
    }
}
