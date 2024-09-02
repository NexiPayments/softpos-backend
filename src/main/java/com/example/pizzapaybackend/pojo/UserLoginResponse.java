package com.example.pizzapaybackend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record UserLoginResponse(
    @Schema(description = "Session id of the new session") @JsonProperty("session_id") UUID sessionId,
    @Schema(description = "Success status of the login process") boolean success
) {
}