package com.bank.acs.dto.challenge.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {

    private String high;
    private String extraHigh;
    private String medium;

}
