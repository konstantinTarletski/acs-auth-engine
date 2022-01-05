package com.bank.acs.dto.banktron;

import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.bank.acs.enumeration.banktron.BanktronStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BanktronLoginDto {

    @NotNull
    @JsonProperty("Status")
    private BanktronStatus status;

    @NotNull
    @JsonProperty("UserName")
    private String username;

    @JsonProperty("LastAuthMethod")
    private BanktronAuthMethod lastAuthMethod;

    @JsonProperty("AuthenticationTypeList")
    private BanktronLoginAuthenticationTypeList authenticationTypesList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BanktronLoginAuthenticationTypeList {

        @JsonProperty("AuthenticationType")
        private List<BanktronLoginAuthenticationTypeDto> authenticationType;

    }

}
