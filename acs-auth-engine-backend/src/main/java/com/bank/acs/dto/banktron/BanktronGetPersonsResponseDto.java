package com.bank.acs.dto.banktron;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BanktronGetPersonsResponseDto extends BanktronResponseDto {

    @JsonProperty("SessionToken")
    private String sessionToken;

    @JsonProperty("PersonList")
    private BanktronPersonList personList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BanktronPersonList {

        @JsonProperty("Person")
        private List<BanktronPersonDto> person;

    }

}
