package com.bank.acs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "transaction_monitor")
public class TransactionMonitor {

    @Id
    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    @Column(name = "acs_transaction_id", nullable = false)
    private String acsTransactionId;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

}
