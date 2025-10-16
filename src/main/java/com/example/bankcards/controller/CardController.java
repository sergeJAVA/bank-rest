package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CreateCardRequest;
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

}
