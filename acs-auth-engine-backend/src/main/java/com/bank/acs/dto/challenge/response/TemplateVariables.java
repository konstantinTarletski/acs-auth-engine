package com.bank.acs.dto.challenge.response;

import com.bank.acs.enumeration.AcsUiType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateVariables {

    private String challengeInfoHeader;
    private String challengeInfoText;
    private String challengeInfoLabel;
    private String challengeInfoTextIndicator;

    private String whyInfoText;
    private String whyInfoLabel;

    private String expandInfoLabel;
    private String expandInfoText;
    private String issuerImageURL;
    private String psImageURL;

    private Image psImage;
    private Image issuerImage;

    private String submitAuthenticationLabel;
    private List<Map<String, String>> challengeSelectInfo;
    private String oobContinueLabel;

    @JsonIgnore
    private AcsUiType acsUiType;

}
