package com.smc.recurring.repository;


import com.smc.recurring.entity.RecurringPaymentEntity;
import jakarta.transaction.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringPaymentDAO extends JpaRepository<RecurringPaymentEntity, String> {

    RecurringPaymentEntity findByCustomerId(String customerId);

    RecurringPaymentEntity findByOrderId(String orderId);

    RecurringPaymentEntity findByContact(String contact);

    RecurringPaymentEntity findByTokenId(String tokenId);

    List<RecurringPaymentEntity> findByFrequency(String frequency);


    List<RecurringPaymentEntity> findAllByOrderByCreatedAtDesc();


    @Modifying
    @Transactional
    @Query("DELETE FROM RecurringPaymentEntity r WHERE r.id NOT IN " +
            "(SELECT MAX(rp.id) FROM RecurringPaymentEntity rp GROUP BY rp.contact)")
    void deleteOldDuplicateTransactions();

}

