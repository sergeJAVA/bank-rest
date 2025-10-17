package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.BalanceRequest;
import com.example.bankcards.dto.request.BlockCardRequest;
import com.example.bankcards.dto.request.ChangeCardStatusRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.DepositMoneyRequest;
import com.example.bankcards.dto.request.TransferMoneyRequest;
import com.example.bankcards.dto.response.ErrorResponse;
import com.example.bankcards.security.TokenAuthentication;
import com.example.bankcards.security.TokenData;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/bank/cards")
@RequiredArgsConstructor
@Tag(name = "Управление картами.", description = "Все операции требуют JWT для доступа к ним.")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание карты с привязкой к пользователю по id", description = "Доступно только с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Переданы невалидные данные.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь с переданным ID.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CardDto> createCard(@RequestBody @Valid CreateCardRequest request) {
        CardDto cardDto = cardService.createCard(request);
        return ResponseEntity.ok(cardDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удаление карты по её ID", description = "Доступно только с ролью ADMIN. ID - Long типа.")
    @ApiResponse(responseCode = "200", description = "Запрос на удаление успешно выполнен.")
    public ResponseEntity<Void> deleteCard(@PathVariable("id") Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    @Operation(summary = "Получение всех карт, которые принадлежат пользователю, с пагинацией.")
    @ApiResponse(responseCode = "200", description = "Запрос успешно выполнен.",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<CardDto>> findAllYourCards(@RequestParam(defaultValue = "0")
                                                          @Parameter(name = "Номер страницы") int page,
                                                      @RequestParam(defaultValue = "10")
                                                      @Parameter(name = "Количество карт на одной странице")int size,
                                                      Authentication authentication) {
        int validPage = Math.max(0, page);
        int validSize = Math.max(0, size);
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        Page<CardDto> cards = cardService.findAllByUserId(tokenData.getId(), validPage, validSize);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Пополнение баланса своей карты.", description = "Пополнить баланс можно только карте" +
            ", которая принадлежит пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен."),
            @ApiResponse(responseCode = "400", description = "Переданы невалидные данные, карта не принадлежит пользователю," +
                    " карта заблокирована или у нёё истёк срок действия.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта с таким номером не найдена.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> depositMoney(@RequestBody @Valid DepositMoneyRequest request,
                                               Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        cardService.depositMoney(tokenData.getId(), request.getCardNum(), request.getAmount());
        return ResponseEntity.ok("The money has been successfully deposited onto the card.");
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод баланса с карты на карту.", description = "Карта, с которой переводятся деньги," +
            " должна принадлежать пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Переданы невалидные данные, карта не принадлежит пользователю," +
                    " карта заблокирована или у нёё истёк срок действия.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта с таким номером не найдена.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> transferMoney(@RequestBody @Valid TransferMoneyRequest request,
                                                Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        cardService.transfer(tokenData.getId(), request.getFromCardNum(), request.getToCardNum(), request.getAmount());
        return ResponseEntity.ok("Money transfer successfully completed.");
    }

    @PostMapping("/changeStatus")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Изменение статуса карты.", description = "Доступно только с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус карты успешно изменён."),
            @ApiResponse(responseCode = "404", description = "Карта не найдена.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> changeCardStatus(@RequestBody @Valid ChangeCardStatusRequest request) {
        cardService.changeStatus(request.getCardId(), request.getCardStatus());
        return ResponseEntity.ok("The status of card with ID <<" + request.getCardId()
                + ">> has been changed to " + request.getCardStatus());
    }

    @PostMapping("/block")
    @Operation(summary = "Запрос за блокировку своей карты.", description = "Карта обязательно должна принадлежать пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос успешно выполнен."),
            @ApiResponse(responseCode = "404", description = "Карта не найдена.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Переданы невалидные данные, карта не принадлежит пользователю," +
                    " карта уже заблокирована или у неё истёк срок действия.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> blockYourCard(@RequestBody @Valid BlockCardRequest request,
                                                Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        cardService.blockCard(tokenData.getId(), request.getCardId());
        return ResponseEntity.ok("The card has been successfully blocked.");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получение всех карт с пагинацией.", description = "Доступно только с ролью ADMIN.")
    @ApiResponse(responseCode = "200", description = "Запрос успешно выполнен.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<CardDto>> findAllCards(@RequestParam(defaultValue = "0")
                                                      @Parameter(name = "Номер страницы") int page,
                                                      @Parameter(name = "Количество карт на одной странице")
                                                      @RequestParam(defaultValue = "10") int size) {
        int validPage = Math.max(0, page);
        int validSize = Math.max(0, size);
        Page<CardDto> cards = cardService.findAll(validPage, validSize);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/balance")
    @Operation(summary = "Получение баланса карты.", description = "Карта должна принадлежать пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен"),
            @ApiResponse(responseCode = "400", description = "Переданы невалидные данные.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена или не принадлежит пользователю.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BigDecimal> getBalance(@RequestBody @Valid BalanceRequest request,
                                                 Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        BigDecimal balance = cardService.getBalance(tokenData.getId(), request.getCardNum());
        return ResponseEntity.ok(balance);
    }

}
