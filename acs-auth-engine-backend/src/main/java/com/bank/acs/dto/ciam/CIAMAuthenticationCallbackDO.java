package com.bank.acs.dto.ciam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CIAMAuthenticationCallbackDO {

    private String type;
    private List<CIAMAuthenticationInputOutputDO> output;
    private List<CIAMAuthenticationInputOutputDO> input;

}
