package com.bank.acs.repository;

import com.bank.acs.entity.TransactionMonitor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionMonitorRepository extends CrudRepository<TransactionMonitor, UUID> {

    List<TransactionMonitor> findAllByAcsTransactionId(String acsTransactionId);

}
