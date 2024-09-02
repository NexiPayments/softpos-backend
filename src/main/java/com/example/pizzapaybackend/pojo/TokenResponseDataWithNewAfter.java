package com.example.pizzapaybackend.pojo;

import com.example.pizzapaybackend.pojo.nexi.TokenResponseData;
import java.time.ZonedDateTime;

public record TokenResponseDataWithNewAfter(
    TokenResponseData tokenResponse,
    ZonedDateTime newAfter
) {

}
