package com.bank.acs.dto.banktron;

import com.bank.acs.enumeration.banktron.BanktronStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.bank.acs.util.CardUtil.maskPersonalCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BanktronPersonDto {

    @NotNull
    @JsonProperty("Status")
    private BanktronStatus status;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @ToString.Exclude
    @JsonProperty("PersonCode")
    private String personCode;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("LoginList")
    private BanktronLoginList loginList;

    @ToString.Include
    private String personCode() {
        return maskPersonalCode(personCode);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BanktronLoginList {

        @JsonProperty("Login")
        private List<BanktronLoginDto> login;

    }

}
