package com.satwik.splitora.settlement.model;

import lombok.Data;

@Data
public class Transaction<U> {

    private U debtor;
    private U creditor;
    private Double amount;

    public Transaction(U debtor, U creditor, Double amount) {
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
    }
}
