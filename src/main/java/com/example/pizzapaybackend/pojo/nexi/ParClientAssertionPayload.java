package com.example.pizzapaybackend.pojo.nexi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

/**
 * Payload of the client assertion
 * appId: copy of the client id
 * pointOfSale: point of sale of the current user
 * terminal_ids: list of terminals assigned to the user (must be one or two terminals)
 * app_username_merchant: username of the user
 * app_deviceid: device id (from SDK)
 * iss: copy of the client id
 * sub: copy of the client id
 * aud: base url of the nexi server https://stgb2bsoftpos.nexigroup.com for staging or https://b2bsoftpos.nexigroup.com for production
 * iat: unix time of creation
 * exp: unix time of expiration
 * jti: unique id of the assertion
 */
@Builder
public record ParClientAssertionPayload(
    @JsonProperty("appId") String appId,
    @JsonProperty("pointOfSale") String pointOfSale,
    @JsonProperty("terminal_ids") List<String> terminalIds,
    @JsonProperty("app_username_merchant") String appUsernameMerchant,
    @JsonProperty("app_deviceid") String appDeviceid,
    @JsonProperty("iss") String iss,
    @JsonProperty("sub") String sub,
    @JsonProperty("aud") String aud,
    @JsonProperty("iat") long iat,
    @JsonProperty("exp") long exp,
    @JsonProperty("jti") String jti
) {

}
