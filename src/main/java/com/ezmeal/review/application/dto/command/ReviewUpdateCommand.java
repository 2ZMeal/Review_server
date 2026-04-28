package com.ezmeal.review.application.dto.command;

import com.ezmeal.common.enums.Role;
import java.util.UUID;

public record ReviewUpdateCommand(
        UUID reviewId,
        String userId,
        Role role,
        String nickname,
        int score,
        String contents
) {
}
