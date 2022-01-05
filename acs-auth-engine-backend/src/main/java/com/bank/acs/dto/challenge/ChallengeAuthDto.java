package com.bank.acs.dto.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeAuthDto {

    private String phone;
    @JsonProperty("authentication_key")
    private String authenticationKey;
    private String otp;
    private String lang;
    private String acsPanRange;
    private Long otpGeneratedDateTime;

    private List<String> mobile;
}
