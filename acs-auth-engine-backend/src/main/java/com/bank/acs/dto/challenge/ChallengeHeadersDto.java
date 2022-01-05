package com.bank.acs.dto.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeHeadersDto {

    @JsonProperty("sec-fetch-mode")
    private String secFetchMode;

    @JsonProperty("sec-fetch-site")
    private String secFetchSite;

    @JsonProperty("sec-fetch-dest")
    private String secFetchDest;

    @JsonProperty("accept-language")
    private String acceptLanguage;

    @JsonProperty("accept-encoding")
    private String acceptEncoding;

    @JsonProperty("upgrade-insecure-requests")
    private String upgradeInsecureRequests;

    @JsonProperty("content-length")
    private String contentLength;

    @JsonProperty("content-type")
    private String contentType;

    @JsonProperty("cache-control")
    private String cacheControl;

    @JsonProperty("user-agent")
    private String userAgent;

    private String referer;
    private String cookie;
    private String origin;
    private String accept;
    private String host;
    private String connection;
}
