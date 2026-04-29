package com.ezmeal.review.application.dto.command;

import com.ezmeal.common.enums.Role;
import java.util.UUID;

public record ReviewDeleteCommand(
        UUID reviewId,
        String userId,
        Role role
) {
}
