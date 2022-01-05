package com.bank.acs.dto.banktron;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BanktronAuthenticationTypeName {

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("Value")
    private String value;

}
