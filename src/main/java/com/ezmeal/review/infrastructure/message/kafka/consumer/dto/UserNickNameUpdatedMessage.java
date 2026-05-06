package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserNickNameUpdatedMessage(
        String userId,

        @JsonProperty("nickname")
        String userNickname,

        LocalDateTime occurredAt
) {}
