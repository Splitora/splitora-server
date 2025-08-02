package com.satwik.splitora.persistence.entities;

import com.satwik.splitora.validator.EmailOrPhoneRequired;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EmailOrPhoneRequired
@Entity
@Table(name = "unregistered_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"phone_country_code", "phone_number"})
})
@Check(constraints = "email IS NOT NULL OR (phone_country_code IS NOT NULL AND phone_number IS NOT NULL)")
public class UnregisteredUser extends BaseEntity {

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_country_code")
    private String countryCode;

    @Column(name = "phone_number")
    private long phoneNumber;

    @OneToMany(mappedBy = "unregisteredMember", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMembers> groupMembers = new ArrayList<>();

}
