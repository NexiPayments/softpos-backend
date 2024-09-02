package com.example.pizzapaybackend.services;

import com.example.pizzapaybackend.configfile.NexiSoftPosBackendConfig;
import com.example.pizzapaybackend.pojo.TokenResponseDataWithNewAfter;
import com.example.pizzapaybackend.pojo.nexi.ParResponse;
import com.example.pizzapaybackend.pojo.nexi.TokenResponseData;
import com.example.pizzapaybackend.utils.RestTemplateWithMtls;
import com.nimbusds.jose.jwk.JWKSet;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NexiUpstreamService {

    private final NexiSoftPosBackendConfig nexiSoftPosBackendConfig;

    private final RestTemplate restTemplate;

    private final ClientAssertionService clientAssertionService;

    private final AtomicReference<TokenResponseDataWithNewAfter> lastTokenResponseData = new AtomicReference<>();

    public NexiUpstreamService(
        final NexiSoftPosBackendConfig nexiSoftPosBackendConfig,
        final ClientAssertionService clientAssertionService
    ) {
        this.nexiSoftPosBackendConfig = nexiSoftPosBackendConfig;
        this.clientAssertionService = clientAssertionService;

        restTemplate = RestTemplateWithMtls.createRestTemplate(
            nexiSoftPosBackendConfig.getPrivateKey(),
            nexiSoftPosBackendConfig.getMtlsCertificate()
        );
    }

    // obtain the current oauth2 access token
    // if there is a valid one will use that, else will require a fresh token
    private String currentAccessToken() {
        final ZonedDateTime now = ZonedDateTime.now();

        final Optional<TokenResponseDataWithNewAfter> optionalTokenResponseDataWithNewAfter = Optional.ofNullable(lastTokenResponseData.get())
            .stream().filter(x -> x.newAfter().isAfter(now)).findFirst();
        if (optionalTokenResponseDataWithNewAfter.isPresent()) {
            return optionalTokenResponseDataWithNewAfter.get().tokenResponse().accessToken();
        }

        if (nexiSoftPosBackendConfig.getBaseUrl() == null) {
            throw new IllegalStateException("null value for base url in obtainNewToken");
        }
        if (nexiSoftPosBackendConfig.getClientId() == null) {
            throw new IllegalStateException("null value for client id in obtainNewToken");
        }
        if (nexiSoftPosBackendConfig.getSecret() == null) {
            throw new IllegalStateException("null value for secret in obtainNewToken");
        }
        final String tokenUrl = nexiSoftPosBackendConfig.getBaseUrl() + "/softpos/appenrollment/token";
        log.info(
            "tokenUrl = {}",
            tokenUrl
        );

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(
            "grant_type",
            "client_credentials"
        );
        map.add(
            "client_id",
            nexiSoftPosBackendConfig.getClientId()
        );
        map.add(
            "client_secret",
            nexiSoftPosBackendConfig.getSecret()
        );

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        final ResponseEntity<TokenResponseData> response = restTemplate.postForEntity(
            tokenUrl,
            request,
            TokenResponseData.class
        );

        final TokenResponseData tokenResponseData = response.getBody();
        if (tokenResponseData == null) {
            throw new IllegalStateException("response body is null");
        }
        final ZonedDateTime newAfter = now.plusSeconds((tokenResponseData.expiresIn() * 100) / 75);

        log.info(
            "next token request after {}",
            newAfter
        );

        lastTokenResponseData.set(new TokenResponseDataWithNewAfter(tokenResponseData, newAfter));

        return tokenResponseData.accessToken();
    }

    // download the JWKs from Nexi to perform JWE
    public JWKSet getNexiJwks() {
        if (nexiSoftPosBackendConfig.getBaseUrl() == null) {
            throw new IllegalStateException("null value for base url in getNexiJwks");
        }
        final String jwksUrl = nexiSoftPosBackendConfig.getBaseUrl() + "/softpos/par/jwks.json";
        log.info(
            "jwksUrl = {}",
            jwksUrl
        );

        final HttpHeaders headers = new HttpHeaders();

        final ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                jwksUrl,
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class
            );
        } catch (final Exception e) {
            log.error(
                "Error: {}",
                e.getMessage(),
                e
            );
            throw new IllegalStateException(e);
        }

        final String body = response.getBody();

        log.info(
            "body = {}",
            body
        );

        try {
            return JWKSet.parse(body);
        } catch (final Exception e) {
            log.warn(
                "Error parsing the response {}: {}",
                body,
                e.getMessage()
            );
            throw new IllegalStateException(e);
        }
    }

    // Execute the par request
    public ParResponse executePar(
        final String pointOfSale,
        final String appUsernameMerchant,
        final String deviceId,
        final String redirectUri,
        final Optional<String> optionalTerminalIdSoftpos,
        final Optional<String> optionalTerminalIdMpos
    ) {
        final JWKSet remoteJWKSet = getNexiJwks();
        if (nexiSoftPosBackendConfig.getBaseUrl() == null) {
            throw new IllegalStateException("null value for base url in executePar");
        }
        final String parUrl = nexiSoftPosBackendConfig.getBaseUrl() + "/softpos/as/par";
        log.info(
            "parUrl = {}",
            parUrl
        );

        // This builds the client assertion value used in the PAR api call
        final String clientAssertion = clientAssertionService.makeClientAssertion(
            pointOfSale,
            appUsernameMerchant,
            deviceId,
            optionalTerminalIdSoftpos,
            optionalTerminalIdMpos,
            remoteJWKSet
        );

        // The par call requires the access token
        final String accessToken = currentAccessToken();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);

        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(
            "client_assertion",
            clientAssertion
        );
        map.add(
            "client_id",
            nexiSoftPosBackendConfig.getClientId()
        );
        map.add(
            "scope",
            "oidc"
        );
        map.add(
            "redirect_uri",
            redirectUri
        );

        log.info(
            "form data map = {}",
            map
        );

        final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        final ResponseEntity<ParResponse> response;
        try {
            response = restTemplate.exchange(
                parUrl,
                HttpMethod.POST,
                entity,
                ParResponse.class
            );
        } catch (final Exception e) {
            log.error(
                "Error: {}",
                e.getMessage(),
                e
            );
            throw new IllegalStateException(e);
        }

        return response.getBody();
    }

}
