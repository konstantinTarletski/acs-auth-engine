package com.bank.acs.exception;

import com.bank.acs.enumeration.AcsErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final AcsErrorCode errorCode;
    private String translatedMessage = null;

    public BusinessException(AcsErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public BusinessException(String translation) {
        super(translation);
        this.errorCode = AcsErrorCode.TRANSLATED_ERROR;
        this.translatedMessage = translation;
    }

    public BusinessException(AcsErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
