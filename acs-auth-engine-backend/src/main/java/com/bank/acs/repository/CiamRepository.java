package com.bank.acs.repository;

import com.bank.acs.entity.ciam.CiamData;
import org.springframework.data.repository.CrudRepository;

public interface CiamRepository extends CrudRepository<CiamData, String> {


}
