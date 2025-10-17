package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.NumberEncryptionUtil;

public class CardMapper {

    public static CardDto toDto(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .cardNum(NumberEncryptionUtil.encryptCardNumber(card.getCardNum()))
                .owner(card.getOwner())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus().toString())
                .balance(card.getBalance())
                .build();
    }

}
