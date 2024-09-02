package com.example.pizzapaybackend.webrouters;

import com.example.pizzapaybackend.entities.UserSessionEntity;
import com.example.pizzapaybackend.pojo.AndroidAppConfig;
import com.example.pizzapaybackend.pojo.AndroidAppUserConfig;
import com.example.pizzapaybackend.services.AndroidAppConfigService;
import com.example.pizzapaybackend.services.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "android-configuration", description = "Fetches the configuration")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("pizzapay/api/v1/android-app-config")
public class AndroidAppConfigRouter {

    private final UserService userSessionService;

    private final AndroidAppConfigService androidAppConfigService;

    // Fetch configs for the current session
    // If the user is new will bind a terminal id from the available pool
    // if the available pool is empty an error is returned.
    // Is NOT intended to be used as a reference for production use
    @SecurityRequirement(name = "appLogin")
    @PostMapping("obtain")
    public ResponseEntity<AndroidAppConfig> obtainConfig(
        @Schema(hidden = true) @RequestHeader("Authorization") final String authorizationHeader,
        @Schema(format = "uuid") @RequestHeader("x-request-id") final Optional<String> optionalRequestId
    ) {
        MDC.put(
            "x_request_id",
            optionalRequestId.orElse("DEFAULT-" + UUID.randomUUID().toString())
        );
        try {
            log.info("request for android app configs");

            final Optional<UserSessionEntity> optionalUserSession = userSessionService.lookupSessionByAuthorizationHeader(authorizationHeader);
            if (!optionalUserSession.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            final UserSessionEntity userSession = optionalUserSession.get();
            log.info(
                "found user session {}",
                userSession
            );
            MDC.put(
                "app_user_email",
                userSession.getUser().getEmail()
            );

            AndroidAppConfig.AndroidAppConfigBuilder androidAppConfigBuilder = AndroidAppConfig.builder();
            androidAppConfigBuilder = androidAppConfigBuilder.clientId(androidAppConfigService.getClientId());

            final Optional<AndroidAppUserConfig> optionalAndroidAppUserConfig = androidAppConfigService.setupUser(userSession.getUser());
            if (optionalAndroidAppUserConfig.isPresent()) {
                androidAppConfigBuilder = androidAppConfigBuilder.androidAppUserConfig(optionalAndroidAppUserConfig.get());
                return ResponseEntity.ok().body(androidAppConfigBuilder.build());
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(androidAppConfigBuilder.build());
            }
        } finally {
            MDC.remove("app_user_email");
            MDC.remove("x_request_id");
        }
    }


}
