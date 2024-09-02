package com.example.pizzapaybackend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request from the app: - redirect_uri: is the value provided in the app creation wizard, must be
 * handled by the app - device_id: from the SDK
 */
public record ParRequestFromApp(
    @JsonProperty("redirect_uri") @Schema(example = "pizzapay://confirm-pay") String redirectUri,
    @Schema(format = "uuid") @JsonProperty("device_id") String deviceid
) {
}
