package com.bank.acs.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OldRequestException extends RuntimeException {

    protected final String acsTransactionId;
    protected final UUID transactionId;

    public OldRequestException(String acsTransactionId, UUID transactionId) {
        super("Need to rollback transaction, acsTransactionId = " + acsTransactionId + " transactionId = " + transactionId);
        this.acsTransactionId = acsTransactionId;
        this.transactionId = transactionId;
    }

}
