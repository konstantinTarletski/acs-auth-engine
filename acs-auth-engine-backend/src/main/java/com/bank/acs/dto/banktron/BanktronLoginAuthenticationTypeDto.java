package com.bank.acs.dto.banktron;

import com.bank.acs.enumeration.banktron.BanktronAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BanktronLoginAuthenticationTypeDto {

    @NotNull
    @JsonProperty("id")
    private BanktronAuthMethod authMethod;

    @JsonProperty("Name")
    private List<BanktronAuthenticationTypeName> name;
}
