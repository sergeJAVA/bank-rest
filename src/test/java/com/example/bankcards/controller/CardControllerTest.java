package com.example.bankcards.controller;

import com.example.bankcards.GenerateJwtForTests;
import com.example.bankcards.TestContainer;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.request.BalanceRequest;
import com.example.bankcards.dto.request.BlockCardRequest;
import com.example.bankcards.dto.request.ChangeCardStatusRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.DepositMoneyRequest;
import com.example.bankcards.dto.request.TransferMoneyRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.NumberEncryptionUtil;
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardControllerTest extends TestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenerateJwtForTests generatorJwt;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CardRepository cardRepository;
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
    void createCard_Success() throws Exception {
        CreateCardRequest request = new CreateCardRequest("1111222233334444", adminId);

        String encryptedCardNum = NumberEncryptionUtil.encryptCardNumber(request.getCardNum());

        String token = generatorJwt.generateJwtToken(1L, "ADMIN");

        mockMvc.perform(post("/api/v1/bank/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value(encryptedCardNum));

        request.setCardNum("3232424244445555");
        encryptedCardNum = NumberEncryptionUtil.encryptCardNumber(request.getCardNum());
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value(encryptedCardNum));
    }

    @Test
    void createCard_Failure_InvalidCardNum() throws Exception {
        CreateCardRequest request = new CreateCardRequest("11112222333wrong", adminId);

        String token = generatorJwt.generateJwtToken(1L, "ADMIN");
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error")
                .value("The card number contains characters that are not numbers!"));
    }

    @Test
    void createCard_Failure_UserNotFound() throws Exception {
        CreateCardRequest request = new CreateCardRequest("1111222233334444", 99L);

        String token = generatorJwt.generateJwtToken(1L, "ADMIN");
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error")
                        .value("User with id <<99>> not found!"));
    }

    @Test
    void createCard_Failure_WrongRole() throws Exception {
        CreateCardRequest request = new CreateCardRequest("1111222233334444", 99L);

        String token = generatorJwt.generateJwtToken(1L, "USER");
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You do not have sufficient permissions to access this resource. Required role: ADMIN."));
    }

    @Test
    void deleteCard_Success() throws Exception {
        CreateCardRequest request = new CreateCardRequest("1111222233332222", adminId);
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        String response = mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardDto cardDto = objectMapper.readValue(response, CardDto.class);

        mockMvc.perform(delete("/api/v1/bank/cards/" + cardDto.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Optional<Card> card = cardRepository.findById(cardDto.getId());
        assertThat(card.isEmpty());
    }

    @Test
    void deleteCard_Failure_WrongRole() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "USER");

        mockMvc.perform(delete("/api/v1/bank/cards/" + adminId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You do not have sufficient permissions to access this resource. Required role: ADMIN."));
    }

    @Test
    void findAllYoursCard_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        mockMvc.perform(get("/api/v1/bank/cards")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void findAllYoursCard_Failure() throws Exception {
        mockMvc.perform(get("/api/v1/bank/cards"))
                .andExpect(status().is(401));
    }

    @Test
    void depositMoney_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        CreateCardRequest createRequest1 = new CreateCardRequest("1111222233334444", adminId);
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest1)))
                .andExpect(status().isOk());

        DepositMoneyRequest request = new DepositMoneyRequest(
                "1111222233334444",
                new BigDecimal("500.0")
        );

        mockMvc.perform(post("/api/v1/bank/cards/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("The money has been successfully deposited onto the card."));
    }

    @Test
    void transferMoney_Success() throws Exception {

        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        TransferMoneyRequest request = new TransferMoneyRequest(
                "1111222233334444",
                "3232424244445555",
                new BigDecimal("150.0")
        );

        CreateCardRequest createRequest1 = new CreateCardRequest("1111222233334444", adminId);
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest1)))
                .andExpect(status().isOk());

        CreateCardRequest createRequest2 = new CreateCardRequest("3232424244445555", adminId);
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest2)))
                .andExpect(status().isOk());

        DepositMoneyRequest depositMoneyRequest = new DepositMoneyRequest(
                "1111222233334444",
                new BigDecimal("500.0")
        );

        mockMvc.perform(post("/api/v1/bank/cards/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositMoneyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("The money has been successfully deposited onto the card."));

        mockMvc.perform(post("/api/v1/bank/cards/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Money transfer successfully completed."));
    }

    @Test
    void changeStatus_Success() throws Exception{
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        CreateCardRequest createRequest = new CreateCardRequest("1111222233334444", adminId);
        String response = mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CardDto cardDto = objectMapper.readValue(response, CardDto.class);

        ChangeCardStatusRequest changeCardStatusRequest = new ChangeCardStatusRequest(cardDto.getId(), CardStatus.BLOCKED);
        mockMvc.perform(post("/api/v1/bank/cards/changeStatus")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeCardStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("The status of card with ID <<" + cardDto.getId()  + ">> has been changed to " +
                        changeCardStatusRequest.getCardStatus()));
    }

    @Test
    void blockYourCard_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        CreateCardRequest createRequest = new CreateCardRequest("1111222233334444", userId);
        String response = mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CardDto cardDto = objectMapper.readValue(response, CardDto.class);

        token = generatorJwt.generateJwtToken(userId, "USER");
        BlockCardRequest blockCardRequest = new BlockCardRequest(cardDto.getId());
        mockMvc.perform(post("/api/v1/bank/cards/block")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockCardRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("The card has been successfully blocked."));

    }

    @Test
    void findAllCards_Success() throws Exception {
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        CreateCardRequest createRequest = new CreateCardRequest("1111222233334444", userId);
        String response = mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CreateCardRequest createRequest2 = new CreateCardRequest("1111222233334242", userId);
        String response2 = mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String response3 = mockMvc.perform(get("/api/v1/bank/cards/all")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(response3);

        JsonNode contentNode = rootNode.get("content");

        TypeReference<List<CardDto>> listCardDtoType = new TypeReference<>() {};

        List<CardDto> cards = objectMapper.readValue(contentNode.toString(), listCardDtoType);
        System.out.println(cards);
        assertThat(cards).hasSize(2);
    }

    @Test
    void getBalance_Success() throws Exception{
        String token = generatorJwt.generateJwtToken(adminId, "ADMIN");

        CreateCardRequest createRequest = new CreateCardRequest("1111222233334444", adminId);
        mockMvc.perform(post("/api/v1/bank/cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        DepositMoneyRequest depositMoneyRequest = new DepositMoneyRequest(
                "1111222233334444",
                new BigDecimal("1000.0")
        );

        mockMvc.perform(post("/api/v1/bank/cards/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositMoneyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("The money has been successfully deposited onto the card."));

        BalanceRequest balanceRequest = new BalanceRequest("1111222233334444");
        mockMvc.perform(get("/api/v1/bank/cards/balance")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceRequest)))
                .andExpect(jsonPath("$").value(depositMoneyRequest.getAmount()));
    }

}