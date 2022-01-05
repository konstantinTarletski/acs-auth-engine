package com.bank.acs.repository;

import com.bank.acs.entity.AppSession;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppSessionRepository extends CrudRepository<AppSession, String> {

    List<AppSession> findAllByUpdatedBefore(LocalDateTime beforeTime);

}
