package com.satwik.splitora.service.interfaces;

import com.satwik.splitora.persistence.dto.expense.ExpenseDTO;
import com.satwik.splitora.persistence.dto.expense.ExpenseMembersRequest;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {
    ExpenseDTO createNonGroupedExpense(ExpenseDTO expenseDTO);

    ExpenseDTO createGroupedExpense(UUID groupId, ExpenseDTO expenseDTO);

    String deleteExpenseById(UUID expenseId);

    String addOwersToExpense(UUID expenseId, ExpenseMembersRequest expenseMembersRequest);

    String removeUserFromExpense(UUID expenseId, UUID owerId);

    ExpenseDTO findExpenseById(UUID expenseId);

    List<ExpenseDTO> findAllExpense(UUID groupId);

}
