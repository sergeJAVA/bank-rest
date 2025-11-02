package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.service.kafka.KafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaService kafkaService;

    @PostMapping
    public ResponseEntity<String> sendMessageToKafka(@RequestBody UserLoginRequest request) {
        UserDto userDto = new UserDto(1L, "Serega", request.getUsername(), null);
        kafkaService.sendMessage("main", request.getPassword(), userDto);
        return ResponseEntity.ok("Сообщение отправлено!");
    }

}
