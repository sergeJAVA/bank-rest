package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.BalanceRequest;
import com.example.bankcards.dto.request.BlockCardRequest;
import com.example.bankcards.dto.request.ChangeCardStatusRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.DepositMoneyRequest;
import com.example.bankcards.dto.request.TransferMoneyRequest;
import com.example.bankcards.security.TokenAuthentication;
import com.example.bankcards.security.TokenData;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@RequestBody @Valid CreateCardRequest request) {
        CardDto cardDto = cardService.createCard(request);
        return ResponseEntity.ok(cardDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable("id") Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> findAllYourCards(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        Page<CardDto> cards = cardService.findAllByUserId(tokenData.getId(), page, size);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> depositMoney(@RequestBody @Valid DepositMoneyRequest request,
                                               Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        cardService.depositMoney(tokenData.getId(), request.getCardNum(), request.getAmount());
        return ResponseEntity.ok("The money has been successfully deposited onto the card.");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody @Valid TransferMoneyRequest request,
                                                Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        cardService.transfer(tokenData.getId(), request.getFromCardNum(), request.getToCardNum(), request.getAmount());
        return ResponseEntity.ok("Money transfer successfully completed.");
    }

    @PostMapping("/changeStatus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeCardStatus(@RequestBody @Valid ChangeCardStatusRequest request) {
        cardService.changeStatus(request.getCardId(), request.getCardStatus());
        return ResponseEntity.ok("The status of card with ID <<" + request.getCardId()
                + ">> has been changed to " + request.getCardStatus());
    }

    @PostMapping("/block")
    public ResponseEntity<String> blockYourCard(@RequestBody @Valid BlockCardRequest request,
                                                Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        cardService.blockCard(tokenData.getId(), request.getCardId());
        return ResponseEntity.ok("The card has been successfully blocked.");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> findAllCards(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Page<CardDto> cards = cardService.findAll(page, size);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestBody @Valid BalanceRequest request,
                                                 Authentication authentication) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
        TokenData tokenData = tokenAuthentication.getTokenData();
        BigDecimal balance = cardService.getBalance(tokenData.getId(), request.getCardNum());
        return ResponseEntity.ok(balance);
    }

}
