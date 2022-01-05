package com.bank.acs.dto.ccc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CCCLanguageDO {

    private String settingCode;
    private String settingPayload;

}
