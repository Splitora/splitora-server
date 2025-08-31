package com.satwik.splitora.persistence.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceTransaction {

    private UUID groupId;
    private String groupName;
    private List<UserTransaction> transactions;
}
