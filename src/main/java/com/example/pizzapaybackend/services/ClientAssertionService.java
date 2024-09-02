package com.example.pizzapaybackend.services;

import com.example.pizzapaybackend.configfile.NexiSoftPosBackendConfig;
import com.example.pizzapaybackend.pojo.nexi.ParClientAssertionPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ClientAssertionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NexiSoftPosBackendConfig nexiSoftPosBackendConfig;

    private final SecureRandom random = new SecureRandom();

    /**
     * This function creates the client assertion ready to be sent
     */
    public String makeClientAssertion(
        final String pointOfSale,
        final String appUsernameMerchant,
        final String deviceId,
        final Optional<String> optionalterminalIdSoftpos,
        final Optional<String> optionalterminalIdMpos,
        final JWKSet remoteJWKSet
    ) {
        final long unixTime = ZonedDateTime.now().toInstant().getEpochSecond();
        // iat and exp must be a valid timestamp
        // use NTP to avoid time synchronization issues
        // The claim will be rejected if the time is wrong
        final long iat = unixTime;
        // this must be short expiring, 500s is planty to perform the action
        final long exp = unixTime + 500;
        final UUID jti = UUID.randomUUID();
        log.info(
            "issuing client assertion with jti = {} at {}",
            jti,
            iat
        );

        final String aud = nexiSoftPosBackendConfig.getBaseUrl();
        log.info(
            "audience for the assertion is {}",
            aud
        );

        // build the list of tids that will be used by the device
        // we can send 1 or 2 tids
        // one can be for mpos
        // one can be for softpos
        // we can't send both of one kind
        final List<String> listOfAllTids = new ArrayList<>();
        optionalterminalIdSoftpos.stream().forEach(listOfAllTids::add);
        optionalterminalIdMpos.stream().forEach(listOfAllTids::add);
        log.info(
            "the terminals assigned to the user are: {}",
            listOfAllTids
        );

        ParClientAssertionPayload.ParClientAssertionPayloadBuilder builder = ParClientAssertionPayload.builder();
        builder = builder.appId(nexiSoftPosBackendConfig.getClientId());
        builder = builder.pointOfSale(pointOfSale);
        builder = builder.terminalIds(listOfAllTids);
        builder = builder.appUsernameMerchant(appUsernameMerchant);
        builder = builder.appDeviceid(deviceId);
        builder = builder.iss(nexiSoftPosBackendConfig.getClientId());
        builder = builder.sub(nexiSoftPosBackendConfig.getClientId());
        builder = builder.aud(aud);
        builder = builder.iat(iat);
        builder = builder.exp(exp);
        builder = builder.jti(jti.toString());
        final ParClientAssertionPayload parClientAssertion = builder.build();
        final String parClientAssertionBodyJson;
        try {
            parClientAssertionBodyJson = objectMapper.writeValueAsString(parClientAssertion);
        } catch (final JsonProcessingException e) {
            log.warn(
                "can't json encode the value: {}",
                parClientAssertion
            );
            throw new IllegalStateException(e);
        }
        log.info(
            "par client assertion: {}",
            parClientAssertionBodyJson
        );

        try {
            // First the payload is signed whit the private key of the application
            final JWK jwkSignature = JWK.parseFromPEMEncodedObjects(nexiSoftPosBackendConfig.getPrivateKey());
            final JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
            final JWSObject jws = new JWSObject(jwsHeader, new Payload(parClientAssertionBodyJson));
            jws.sign(new RSASSASigner(jwkSignature.toRSAKey()));
            final String jwsPayload = jws.serialize();

            // The signed data is encrypted using one of the keys from nexi
            final JWK jwkEncryption = remoteJWKSet.getKeys().get(random.nextInt(remoteJWKSet.getKeys().size()));
            final EncryptionMethod encryptionMethod = EncryptionMethod.A128CBC_HS256;
            final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(encryptionMethod.cekBitLength());
            final SecretKey contentEncryptionKey = keyGenerator.generateKey();
            final RSAPublicKey rsaPublicKey = (RSAPublicKey) jwkEncryption.toPublicJWK().toRSAKey().toPublicKey();
            final JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, encryptionMethod);
            final JWEObject jwe = new JWEObject(jweHeader, new Payload(jwsPayload));
            jwe.encrypt(new RSAEncrypter(rsaPublicKey, contentEncryptionKey));
            return jwe.serialize();
        } catch (final Exception e) {
            log.warn(
                "error in cryptography: {}",
                e.getMessage(),
                e
            );
            throw new IllegalStateException(e);
        }
    }

}
