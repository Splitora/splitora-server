package com.satwik.splitora.persistence.dto.group;

import lombok.Data;

import java.util.UUID;

@Data
public class UserTransaction {

    private UUID debtorId;

    private String debtorName;

    private UUID creditorId;

    private String creditorName;

    private Double amount;
}
