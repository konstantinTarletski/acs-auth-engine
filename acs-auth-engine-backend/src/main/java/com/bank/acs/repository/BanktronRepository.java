package com.bank.acs.repository;

import com.bank.acs.entity.banktron.BanktronData;
import org.springframework.data.repository.CrudRepository;

public interface BanktronRepository extends CrudRepository<BanktronData, String> {


}
