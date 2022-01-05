package com.bank.acs.dto.challenge.request;

import com.bank.acs.dto.challenge.ChallengeAuthDto;
import com.bank.acs.dto.challenge.ChallengeHeadersDto;
import com.bank.acs.enumeration.AcsAuthenticationMethod;
import com.bank.acs.enumeration.ChallengeMethod;
import com.bank.acs.enumeration.DeviceChannel;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.bank.acs.util.CardUtil.maskCardNumber;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeRequestDto {

    /**
     * 3DS Server Transaction ID, universally unique transaction identifier assigned by the 3DS Server to identify a single transaction
     */
    @NotEmpty
    @Size(max = 36)
    private String threeDSServerTransID;

    /**
     * ACS Transaction ID, universally unique transaction identifier assigned by the ACS to identify a single transaction
     */
    @NotEmpty
    @Size(max = 36)
    private String acsTransID;

    /**
     * DS Transaction ID, universally unique transaction identifier assigned by the DS to identify a single transaction
     */
    @NotEmpty
    @Size(max = 36)
    private String dsTransID;

    /**
     * URL that received the request
     */
    @NotEmpty
    private String url;

    /**
     * The method how this request was received
     */
    @NotNull
    private ChallengeMethod method;

    /**
     * Content that was received in case of "POST" method
     */
    private String content;

    /**
     * Device Channel used to initiate the transaction
     */
    @NotNull
    private DeviceChannel deviceChannel;

    /**
     * Cardholder Account Number that will be used in the authorisation request for payment transaction
     */
    @ToString.Exclude
    @NotEmpty
    @Size(min = 13, max = 19)
    private String acctNumber;

    /**
     * Card/Token Expiry Date, presence of this field is DS specific
     */
    @ToString.Exclude
    @Size(max = 4)
    private String cardExpiryDate;

    /**
     * Challenge Window Size, one of predefined values.
     * Values accepted: 01 = 250x400; 02 = 390x400; 03 = 500x600; 04 = 600x400; 05 = Full screen
     */
    @Size(max = 2)
    private String challengeWindowSize;

    /**
     * Arbitrary JSON Object used for storing authentication data. May include Cardholder's ID in banking system,
     * phone number for sending OTP in field "phone", preferred language in field "lang, etc
     */
    private ChallengeAuthDto authenticationData;

    /**
     * HTTP headers received in request (JSON Object)
     */
    private ChallengeHeadersDto headers;

    private AcsAuthenticationMethod acsAuthenticationMethod;
    private String dsProtocol;
    private String messageVersion;

    private ChallengeRequestPurchaseDto purchaseData;
    private ChallengeRequestAcsRenderingTypeDto acsRenderingType;

    @ToString.Include
    private String acctNumber() {
        return maskCardNumber(acctNumber);
    }
}
