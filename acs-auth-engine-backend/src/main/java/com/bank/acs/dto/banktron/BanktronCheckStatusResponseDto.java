package com.bank.acs.dto.banktron;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BanktronCheckStatusResponseDto extends BanktronResponseDto {

    /**
     * Signature status
     */
    @JsonProperty("SignedStatus")
    private Boolean signed;

    @JsonProperty("SignedString")
    private String signedString;

}
