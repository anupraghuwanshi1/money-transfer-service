package com.example.demo.repository;

import com.example.demo.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("select t from Transfer t where t.sourceAccount.id = :accountId or t.targetAccount.id = :accountId order by t.createdAt desc")
    List<Transfer> findHistoryForAccount(@Param("accountId") Long accountId);

    @Query("select coalesce(sum(t.amount), 0) from Transfer t where t.sourceAccount.id = :accountId and t.status = com.example.demo.enums.TransferStatus.SUCCESS and t.createdAt >= :startOfDay and t.createdAt < :nextDay")
    BigDecimal sumSuccessfulTransfersForDay(@Param("accountId") Long accountId,
                                          @Param("startOfDay") LocalDateTime startOfDay,
                                          @Param("nextDay") LocalDateTime nextDay);
}
