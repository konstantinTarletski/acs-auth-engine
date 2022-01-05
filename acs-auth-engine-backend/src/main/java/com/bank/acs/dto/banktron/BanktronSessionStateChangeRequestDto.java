package com.bank.acs.dto.banktron;

import com.bank.acs.enumeration.banktron.BanktronSessionState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BanktronSessionStateChangeRequestDto extends BanktronResponseDto {

    @NotNull
    @JsonProperty("SessionToken")
    private String sessionToken;

    @JsonProperty("SessionState")
    private BanktronSessionState sessionState;

    @JsonProperty("SessionStateChangeDate")
    private LocalDateTime sessionStateChangeDate;
}
