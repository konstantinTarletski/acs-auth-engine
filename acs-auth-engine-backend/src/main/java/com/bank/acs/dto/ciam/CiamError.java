package com.bank.acs.dto.ciam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CiamError {

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("error")
    private String error;

}
