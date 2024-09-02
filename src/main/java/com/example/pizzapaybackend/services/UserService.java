package com.example.pizzapaybackend.services;

import com.example.pizzapaybackend.entities.UserEntity;
import com.example.pizzapaybackend.entities.UserSessionEntity;
import com.example.pizzapaybackend.repositories.UserRepository;
import com.example.pizzapaybackend.repositories.UserSessionRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserSessionRepository userSessionRepository;

    // This function will allow any login request that use
    // a password that is the username fllowed by "!"
    // For example user@example.com with user@example.com! is a valid user
    // Will add new users to the database if the credentials are OK
    // At each invocation will respond with a UUID that represents the session of the user
    // Is NOT intended to be used as a reference for production use
    @Transactional
    public Optional<UUID> newSessionFor(
        final String email,
        final String password
    ) {
        if (email == null) {
            log.info("email is null");
            return Optional.empty();
        }
        if (password == null) {
            log.info("password is null");
            return Optional.empty();
        }
        final String targetPassword = email + "!";
        if (!password.equalsIgnoreCase(targetPassword)) {
            log.info(
                "email's password for {} must be {}",
                email,
                targetPassword
            );
            return Optional.empty();
        }

        final Optional<UserEntity> optionalUser = userRepository.findOne(
            (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(
                    root.get("email"),
                    email
                )
            )
        );
        final UserEntity user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new UserEntity();
            user.setEmail(email);
            userRepository.save(user);
        }

        final UserSessionEntity userSession = new UserSessionEntity();
        userSession.setUserId(user.getId());
        userSessionRepository.save(userSession);
        return Optional.of(userSession.getId());
    }


    // lookup the session by the authorization header
    public Optional<UserSessionEntity> lookupSessionByAuthorizationHeader(
        final String authorizationHeader
    ) {
        final String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            return Optional.empty();
        }

        final String token = authorizationHeader.substring(prefix.length());

        try {
            final UUID sessionId = UUID.fromString(token);

            return userSessionRepository.findById(sessionId);
        } catch (final Exception e) {
            log.info(
                "error searching for session id: {}",
                e.getMessage()
            );
            return Optional.empty();
        }
    }

}
