package com.satwik.splitora.persistence.dto.group;

import com.satwik.splitora.persistence.dto.user.PhoneDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDTO {

    UUID groupMemberId;

    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    String name;

    String username;

    String email;

    PhoneDTO phone;
}
