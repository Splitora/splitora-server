package com.satwik.splitora.persistence.dto.expense;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExpenseMembersRequest {

    @NotNull (message = "Expense ID should not be null")
    private List<UUID> membersId;
}
