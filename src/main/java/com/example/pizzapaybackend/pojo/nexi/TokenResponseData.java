package com.example.pizzapaybackend.pojo.nexi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OAuth2 token response payload
 */
public record TokenResponseData(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") Integer expiresIn,
    @JsonProperty("token_type") String tokenType
) {
}
