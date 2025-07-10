package com.satwik.splitora.settlement.strategy;

import com.satwik.splitora.settlement.model.Transaction;
import lombok.Data;

import java.util.*;

public class SortSettlementStrategy<U> implements SettlementStrategy<U> {

    @Data
    private class Person {
        private U id;
        private Double balance;

        public Person(U id, Double balance) {
            this.id = id;
            this.balance = balance;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(id, person.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Override
    public List<Transaction<U>> settle(List<Transaction<U>> transactions) {
        HashMap<U, Double> balances = new HashMap<>();
        for (Transaction<U> transaction : transactions) {
            U creditor = transaction.getCreditor();
            U debtor = transaction.getDebtor();
            double amount = transaction.getAmount();

            balances.put(creditor, balances.getOrDefault(creditor, 0.0) + amount);
            balances.put(debtor, balances.getOrDefault(debtor, 0.0) - amount);
        }

        // Create lists for creditors and debtors
        List<Person> creditors = new ArrayList<>();
        List<Person> debtors = new ArrayList<>();

        for (var entry : balances.entrySet()) {
            U personId = entry.getKey();
            Double balance = entry.getValue();

            if (balance < 0) {
                creditors.add(new Person(personId, balance));
            } else if (balance > 0) {
                debtors.add(new Person(personId, balance));
            }
        }

        creditors.sort(Comparator.comparingDouble(Person::getBalance));
        debtors.sort(Comparator.comparingDouble(Person::getBalance));

        List<Transaction<U>> settlingTransactions = new ArrayList<>();

        int i = 0;
        int j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            Person creditor = creditors.get(i);
            Person debtor = debtors.get(j);

            double minAmount = Math.min(creditor.balance, debtor.balance);
            settlingTransactions.add(new Transaction<>(debtor.getId(), creditor.getId(), Math.abs(minAmount)));

            creditor.balance -= minAmount;
            debtor.balance -= minAmount;

            if (creditor.balance == 0) i++;
            if (debtor.balance == 0) j++;
        }

        return settlingTransactions;
    }
}
