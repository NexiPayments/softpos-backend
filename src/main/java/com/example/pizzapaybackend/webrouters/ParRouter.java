package com.example.pizzapaybackend.webrouters;

import com.example.pizzapaybackend.entities.UserSessionEntity;
import com.example.pizzapaybackend.pojo.ParRequestFromApp;
import com.example.pizzapaybackend.pojo.nexi.ParResponse;
import com.example.pizzapaybackend.services.NexiUpstreamService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "par", description = "Execute the PAR api call")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("pizzapay/api/v1/par")
public class ParRouter {

    private final UserService userSessionService;

    private final NexiUpstreamService nexiUpstreamService;

    // This call will trigger an upstream call to Nexi
    // in order to bootstrap 
    // Is NOT intended to be used as a reference for production use
    @SecurityRequirement(name = "appLogin")
    @PostMapping("execute")
    public ResponseEntity<ParResponse> executePar(
        @Schema(hidden = true) @RequestHeader("Authorization") final String authorizationHeader,
        @Schema(format = "uuid") @RequestHeader("x-request-id") final Optional<String> optionalRequestId,
        @RequestBody final ParRequestFromApp parRequestFromApp
    ) {
        MDC.put(
            "x_request_id",
            optionalRequestId.orElse("DEFAULT-" + UUID.randomUUID().toString())
        );
        try {
            log.info(
                "par request with app payload {}",
                parRequestFromApp
            );

            final Optional<UserSessionEntity> optionalUserSession = userSessionService.lookupSessionByAuthorizationHeader(authorizationHeader);
            if (!optionalUserSession.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            final UserSessionEntity userSession = optionalUserSession.get();
            MDC.put(
                "app_user_email",
                userSession.getUser().getEmail()
            );
            log.info(
                "found user session {}",
                userSession
            );

            /**
             * Using the current user data the system calls nexi for par request
             */
            final ParResponse parResponse = nexiUpstreamService.executePar(
                userSession.getUser().getPointOfSale(),
                userSession.getUser().getEmail(),
                parRequestFromApp.deviceid(),
                parRequestFromApp.redirectUri(),
                Optional.ofNullable(userSession.getUser().getTerminalIdSoftpos()),
                Optional.ofNullable(userSession.getUser().getTerminalIdMpos())
            );
            log.info(
                "par response = {}",
                parResponse
            );
            return ResponseEntity.ok().body(parResponse);
        } finally {
            MDC.remove("app_user_email");
            MDC.remove("x_request_id");
        }

    }

}
