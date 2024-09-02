package com.example.pizzapaybackend.pojo.nexi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response from par API - request_uri is the value to be used by the mobile app - expires_in is how
 * long the value is valid, in seconds
 */
public record ParResponse(
    @Schema(example = "urn:softpos_a86dd649-3a0d-4c0c-ad9e-c0aa58058d18") @JsonProperty("request_uri") String requestUri,
    @Schema(example = "90") @JsonProperty("expires_in") Integer expiresIn
) {
}
