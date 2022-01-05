package com.bank.acs.dto.challenge.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeRequestAcsRenderingTypeDto {

    private String acsUiTemplate;
    private String acsInterface;
}
