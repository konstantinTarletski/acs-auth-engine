package com.bank.acs.entity.ciam;

import com.bank.acs.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ciam_data")
public class CiamData extends BaseEntity {

    @Id
    @Column(name = "acs_transaction_id", nullable = false)
    private String acsTransactionId;

    @Column(name = "authenticate_response", length = 10000)
    private String authenticateResponse;

}
