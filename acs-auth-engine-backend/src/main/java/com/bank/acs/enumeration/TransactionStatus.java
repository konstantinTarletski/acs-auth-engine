package com.bank.acs.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {

    /**
     * Authentication / Account Verification Successful
     */
    VERIFICATION_SUCCESSFUL("Y"),

    /**
     * Not Authenticated /Account Not Verified
     */
    TRANSACTION_DENIED("N"),

    /**
     * Authentication/ Account Verification Could Not Be Performed; Technical or other problem, as indicated in ARes or RReq
     */
    TECHNICAL_PROBLEM("U"),

    /**
     * Attempts Processing Performed; Not Authenticated/Verified, but a proof of attempted authentication/verification is provided
     */
    ATTEMPTS_PROCESSING_PREFORMED("A"),

    /**
     * Challenge Required; Additional authentication is required using the CReq/CRes
     */
    CHALLENGE_REQUIRED("C"),

    /**
     * Authentication/ Account Verification Rejected; Issuer is rejecting authentication/verification and request that authorisation not be attempted
     */
    VERIFICATION_REJECTED("R");

    @JsonValue
    private final String status;

    @SuppressWarnings("unused")
    @JsonCreator
    public static TransactionStatus fromStatusLetter(String statusLetter) {
        return Arrays.stream(values()).filter(v -> v.getStatus().equals(statusLetter)).findFirst().orElse(null);
    }
}
