package com.example.pizzapaybackend.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
    @Schema(description = "Email of the operator", example = "user1@example.com") @NotNull @NotEmpty @Email String email,
    @Schema(description = "Password of the operator", example = "user1@example.com!") @NotNull @NotEmpty String password
) {
}