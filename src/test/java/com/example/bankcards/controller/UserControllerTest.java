package com.example.bankcards.controller;

import com.example.bankcards.GenerateJwtForTests;
import com.example.bankcards.TestContainer;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest extends TestContainer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenerateJwtForTests generatorJwt;

    private Long adminId;
    private Long userId;

    @BeforeEach
    void setUpBeforeEach() {
        userRepository.deleteAll();

        User admin = userRepository.findByUsername("ADMIN")
                .orElseGet(() -> {
                    Role adminRole = roleRepository.findByName("ADMIN")
                            .orElseThrow(() -> new RuntimeException("Admin Role not found!"));

                    Set<Role> roles = new HashSet<>();
                    roles.add(adminRole);

                    User newAdmin = User.builder()
                            .username("ADMIN")
                            .fullName("Test Admin")
                            .password("password123")
                            .roles(roles)
                            .build();
                    return userRepository.save(newAdmin);
                });

        User user = createUser();

        adminId = admin.getId();
        userId = user.getId();
    }

    private User createUser() {
        Role adminRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER Role not found!"));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User newUser = User.builder()
                .username("User")
                .fullName("Test User")
                .password("password123")
                .roles(roles)
                .build();
        return userRepository.save(newUser);
    }

    @Test
    void createUser_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(
                "Sergej K",
                "test12",
                "password123"
        );

        mockMvc.perform(post("/api/v1/bank/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test12"));
    }

    @Test
    void createUser_Failure_UserRole() throws Exception {
        String token = generatorJwt.generateJwtToken(userId, "USER");
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(
                "Sergej K",
                "test12",
                "password123"
        );

        mockMvc.perform(post("/api/v1/bank/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.message")
                        .value("You do not have sufficient permissions to access this resource. Required role: ADMIN."));
    }

    @Test
    void deleteUser_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(
                "Sergej K",
                "test12",
                "password123"
        );
        String response = mockMvc.perform(post("/api/v1/bank/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test12"))
                .andReturn().getResponse().getContentAsString();

        UserDto userDto = objectMapper.readValue(response, UserDto.class);
        Optional<User> existingUser = userRepository.findById(userDto.getId());
        assertThat(existingUser.isPresent());

        mockMvc.perform(delete("/api/v1/bank/users/" + userDto.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


        existingUser = userRepository.findById(userDto.getId());
        assertThat(existingUser.isEmpty());
    }

    @Test
    void deleteUser_Failure_UserRole() throws Exception {
        String token = generatorJwt.generateJwtToken(userId, "USER");
        mockMvc.perform(delete("/api/v1/bank/users/" + 1L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                .value("You do not have sufficient permissions to access this resource. Required role: ADMIN."));
    }

    @Test
    void findAll_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(
                "Sergej K",
                "test12",
                "password123"
        );

        mockMvc.perform(post("/api/v1/bank/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test12"));

        String response = mockMvc.perform(get("/api/v1/bank/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(response);

        JsonNode contentNode = rootNode.get("content");

        TypeReference<List<UserDto>> listUserDtoType = new TypeReference<>() {};

        List<UserDto> users = objectMapper.readValue(contentNode.toString(), listUserDtoType);

        UserDto userDto = users.getFirst();
        assertThat(userDto.getFullName().equals("Sergej K"));
    }

    @Test
    void findAll_Failure_UserRole() throws Exception {
        String token = generatorJwt.generateJwtToken(userId, "USER");
        mockMvc.perform(get("/api/v1/bank/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                    .value("You do not have sufficient permissions to access this resource. Required role: ADMIN."));
    }

}