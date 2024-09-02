package com.example.pizzapaybackend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AndroidAppUserConfig(
    @JsonProperty("point_of_sale") String pointOfSale,
    @JsonProperty("terminal_id_softpos") String terminalIdSoftpos,
    @JsonProperty("terminal_id_mpos") String terminalIdMpos
) {

}
