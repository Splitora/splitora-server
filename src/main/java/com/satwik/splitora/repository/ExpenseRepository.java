package com.satwik.splitora.repository;

import com.satwik.splitora.persistence.entities.Expense;
import com.satwik.splitora.settlement.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByGroupId(UUID groupId);

    @Query("SELECT NEW com.satwik.splitora.settlement.model.Transaction("+
            "es.groupMembers.id, " +
            "e.payer.id, " +
            "es.sharedAmount) " +
            "FROM Expense e " +
            "JOIN ExpenseShare es " +
            "ON e.id = es.expense.id " +
            "WHERE e.group.id = ?1 ")
    List<Transaction<UUID>> findTransactionsByGroupId(UUID id);
}
