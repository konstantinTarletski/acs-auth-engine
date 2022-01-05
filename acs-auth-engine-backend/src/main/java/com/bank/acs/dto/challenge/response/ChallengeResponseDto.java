package com.bank.acs.dto.challenge.response;

import com.bank.acs.dto.challenge.ChallengeAuthDto;
import com.bank.acs.dto.challenge.ChallengeHeadersDto;
import com.bank.acs.enumeration.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeResponseDto {

    /**
     * ACS Transaction ID, universally unique transaction identifier assigned by the ACS to identify a single transaction
     */
    @NotNull
    @Size(max = 36)
    private String acsTransID;

    @NotEmpty
    @Size(max = 1)
    private TransactionStatus transStatus;

    /**
     * Transaction Status Reason, provides information on why the Transaction Status field has the specified value
     */
    /*
    This field no not used, but sending `transStatusReason = null` cause problem to visa on
    transStatus = VERIFICATION_SUCCESSFUL (Y)
    Should be fixed, if this field will used in future.
     */
    //
    //@Size(max = 2)
    //private String transStatusReason;

    /**
     * HTTP status code to return
     */
    @NotNull
    @Digits(integer = 3, fraction = 0)
    private String statusCode;

    /**
     * HTTP headers for response
     */
    private ChallengeHeadersDto headers;

    /**
     * Content to return in response
     */
    private String content;

    /**
     * Template name for producing response in ACS
     */
    @Size(max = 256)
    private String templateName;

    private TemplateVariables templateVariables;

    /**
     * User interface type that the 3DS SDK will render,
     * which includes the specific data mapping and requirements.
     * (e.g. 01=Text, 02=Single Select, 03=Multi Select or 04=OOB)
     */
    @Size(max = 2)
    private String acsUiType;

    /**
     * Template language for producing response in ACS
     */
    @Size(max = 2)
    private String templateLanguage;

    /**
     * JSON Object used for storing authentication data. All services may return updated "authenticationData"
     * (e.g. with OTP hash in field "otp") which should be stored and sent in subsequent requests
     */
    private ChallengeAuthDto authenticationData;

    /**
     * Only for test for now
     */
    private String whiteListStatus;

    /**
     * Only for test for now
     */
    private String whitelist;

}
