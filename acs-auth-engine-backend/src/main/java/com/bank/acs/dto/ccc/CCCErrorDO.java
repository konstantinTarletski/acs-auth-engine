package com.bank.acs.dto.ccc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CCCErrorDO {

    private Integer status;
    private String code;
    private String message;
    private String requestId;
    private String detail;
    private String timestamp;

}
