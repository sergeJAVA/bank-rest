package com.example.bankcards.service.kafka;

import com.example.bankcards.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    private final KafkaTemplate<String, UserDto> kafkaTemplate;

    public void sendMessage(String topic, String key, UserDto value) {
        kafkaTemplate.send(topic, key, value);
        LOGGER.info("KafkaTemplate sent the message.");
    }

    @KafkaListener(topics = "main", groupId = "consumer", containerFactory = "containerFactory")
    public void handle(ConsumerRecord<String, UserDto> consumerRecord) {
        UserDto userDto = consumerRecord.value();
        LOGGER.info("Consumer processed message: {}", userDto);
    }

}
