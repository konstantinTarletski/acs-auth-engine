package com.bank.acs.service;

import com.bank.acs.entity.TransactionMonitor;
import com.bank.acs.exception.BusinessException;
import com.bank.acs.repository.TransactionMonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(noRollbackFor = BusinessException.class)
@Service
public class MultiTransactionResolver {

    protected final TransactionMonitorRepository transactionMonitorRepository;

    public void registerTransaction(String acsTransactionId, UUID id) {
        final var time = LocalDateTime.now();
        final var transaction = TransactionMonitor.builder()
                .requestTime(time)
                .acsTransactionId(acsTransactionId)
                .threadId(id)
                .build();

        log.info("registerTransaction, id = {}, acsTransactionId = {}, time = {}", id, acsTransactionId, time);
        transactionMonitorRepository.save(transaction);
    }

    public boolean isTransactionToRollback(String acsTransactionId, UUID id) {
        final var openedTransactions = transactionMonitorRepository.findAllByAcsTransactionId(acsTransactionId);

        List<TransactionMonitor> sortedList = openedTransactions.stream()
                .sorted(Comparator.comparing(TransactionMonitor::getRequestTime).reversed())
                .collect(Collectors.toList());

        final var transactionOptional = sortedList.stream().filter(item -> item.getThreadId().equals(id))
                .findAny();

        if (transactionOptional.isEmpty()) {
            log.warn("isTransactionToRollback FALSE (no transaction), id = {}, acsTransactionId = {}", id, acsTransactionId);
            return false;
        }

        final var index = sortedList.indexOf(transactionOptional.get());

        if (sortedList.size() > 1 && index > 0) {
            log.info("isTransactionToRollback TRUE, id = {}, acsTransactionId = {}, openedTransactionsSize = {} ", id, acsTransactionId, sortedList.size());
            log.info("openedTransactions = {} ", sortedList);
            return true;
        }
        log.info("isTransactionToRollback FALSE (last transaction), id = {}, acsTransactionId = {}, openedTransactionsSize = {} ", id, acsTransactionId, sortedList.size());
        return false;
    }

    public void deleteTransactionMonitor(UUID id) {
        transactionMonitorRepository.deleteById(id);
        log.info("deleteTransactionMonitor, id = {}", id);
    }

}
