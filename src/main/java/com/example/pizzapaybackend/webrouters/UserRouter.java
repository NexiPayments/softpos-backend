package com.example.pizzapaybackend.webrouters;

import com.example.pizzapaybackend.pojo.UserLoginRequest;
import com.example.pizzapaybackend.pojo.UserLoginResponse;
import com.example.pizzapaybackend.services.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "user", description = "User APIs")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("pizzapay/api/v1/user")
public class UserRouter {

    private final UserService userSessionService;

    // This endpoint expose and example of a login functionality
    // Is NOT intended to be used as a reference for production use
    @PostMapping("login")
    public UserLoginResponse postMethodName(
        @Schema(format = "uuid") @RequestHeader("x-request-id") final Optional<String> optionalRequestId,
        @RequestBody final UserLoginRequest userData
    ) {
        MDC.put(
            "x_request_id",
            optionalRequestId.orElse("DEFAULT-" + UUID.randomUUID().toString())
        );
        try {
            log.info(
                "New login for user {}",
                userData.email()
            );
            final Optional<UUID> optionalId = userSessionService.newSessionFor(
                userData.email(),
                userData.password()
            );
            if (optionalId.isPresent()) {
                log.info("login OK");
                return new UserLoginResponse(optionalId.get(), true);
            } else {
                log.warn("login KO");
                return new UserLoginResponse(null, false);
            }
        } finally {
            MDC.remove("x_request_id");
        }
    }

}
