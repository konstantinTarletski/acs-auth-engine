package com.bank.acs.dto.ccc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CCCUsernameAndLanguagesDO {

    private String username;
    private List<CCCLanguageDO> languages;

}
