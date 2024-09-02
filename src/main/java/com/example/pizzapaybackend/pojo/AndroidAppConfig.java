package com.example.pizzapaybackend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AndroidAppConfig(
    @JsonProperty("client_id") String clientId,
    @JsonProperty("android_app_user_config") AndroidAppUserConfig androidAppUserConfig
) {

}
