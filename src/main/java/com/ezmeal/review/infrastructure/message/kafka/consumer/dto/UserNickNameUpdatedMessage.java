package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.message.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserNickNameUpdatedMessage(
        String userId,
        String nickname
) implements DomainEvent {}
