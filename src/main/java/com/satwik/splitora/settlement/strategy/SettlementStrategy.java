package com.satwik.splitora.settlement.strategy;

import com.satwik.splitora.settlement.model.Transaction;

import java.util.List;

public interface SettlementStrategy <U> {

    /**
     * Settle a list of transactions and return the settled transactions.
     *
     * @param transactions List of transactions to be settled
     * @return List of settled transactions
     */
    List<Transaction<U>> settle(List<Transaction<U>> transactions);

}
