package com.bank.acs.dto.ciam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CIAMAuthenticationResponseDO {

    private String tokenId;
    private String successUrl;
    private String realm;
    private String authId;
    private List<CIAMAuthenticationCallbackDO> callbacks;

}
