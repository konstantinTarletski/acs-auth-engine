package com.bank.acs.dto.ciam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CIAMAuthenticationInputOutputDO {

    private String name;
    private Object value;

}
