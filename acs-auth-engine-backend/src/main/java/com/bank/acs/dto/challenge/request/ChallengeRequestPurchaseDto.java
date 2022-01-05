package com.bank.acs.dto.challenge.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeRequestPurchaseDto {

    private String purchaseDate;
    private String purchaseCurrency;
    private String merchantName;
    private String purchaseAmount;
}
