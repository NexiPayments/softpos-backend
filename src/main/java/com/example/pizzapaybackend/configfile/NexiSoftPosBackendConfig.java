package com.example.pizzapaybackend.configfile;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nexisoftposbackend", ignoreInvalidFields = false)
public class NexiSoftPosBackendConfig {

    /*
     * Base URL for Nexi servers. Values expected are:
     * - https://stgb2bsoftpos.nexigroup.com for staging
     * - https://b2bsoftpos.nexigroup.com for production
     */
    private String baseUrl;

    /**
     * Client ID of the application created using:
     * - https://stgposweb.nexi.it for staging
     * - https://posweb.nexi.it for production
     */
    private String clientId;

    /**
     * The secret linked to the Client ID, available on the same page
     */
    private String secret;

    /**
     * Certificated signed from Nexi's CA.
     * The CSR must be uploaded in the same portal used to create the client_id/client_secret pair
     * Is used for mTLS
     */
    private String mtlsCertificate;

    /**
     * Private key linked to the certificate.
     * Is used for mTLS and Client Assertion
     */
    private String privateKey;

}
