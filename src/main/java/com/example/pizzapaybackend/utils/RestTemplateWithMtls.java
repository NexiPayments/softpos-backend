package com.example.pizzapaybackend.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RestTemplateWithMtls {

    private RestTemplateWithMtls() {

    }

    public static RestTemplate createRestTemplate(
        final String privateKeyPem,
        final String mtlsCertificate
    ) {
        final SSLConnectionSocketFactory sslConFactory;
        try {
            sslConFactory = buildSslConFactory(
                privateKeyPem,
                mtlsCertificate
            );
        } catch (final Exception e) {
            log.error(
                "Can't bootstrap key material for mTLS: {}",
                e.getMessage(),
                e
            );
            throw new IllegalStateException(e);
        }

        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslConFactory).build();
        final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    private static SSLConnectionSocketFactory buildSslConFactory(
        final String privateKeyPem,
        final String mtlsCertificate
    ) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException, UnrecoverableKeyException, JOSEException {
        final JWK jwk = JWK.parseFromPEMEncodedObjects(privateKeyPem);
        final RSAPrivateKey privateKey = jwk.toRSAKey().toRSAPrivateKey();

        final PemReader certificatePem = new PemReader(
            new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mtlsCertificate.getBytes())))
        );
        final PemObject pemObject = certificatePem.readPemObject();
        if (!"CERTIFICATE".equals(pemObject.getType())) {
            throw new IllegalStateException("invalid pem block type: " + pemObject.getType());
        }
        final InputStream inputStream = new ByteArrayInputStream(pemObject.getContent());
        final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        final Certificate certificateData = certFactory.generateCertificate(inputStream);
        final X509Certificate cert;
        if (certificateData instanceof X509Certificate) {
            cert = (X509Certificate) certificateData;
        } else {
            throw new IllegalStateException("invalid certificate found");
        }

        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(
            null,
            null
        );
        keyStore.setKeyEntry(
            "",
            privateKey,
            null,
            new Certificate[] {cert}
        );

        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder = sslContextBuilder.loadKeyMaterial(
            keyStore,
            null
        );
        final SSLContext sslContext = sslContextBuilder.build();

        return new SSLConnectionSocketFactory(sslContext);
    }


}
