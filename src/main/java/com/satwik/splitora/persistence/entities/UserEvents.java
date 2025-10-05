package com.satwik.splitora.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_events")
public class UserEvents extends BaseEntity {

    @Column(name = "welcome_email_sent", nullable = false)
    private boolean welcomeEmailSent = false;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_number_verified", nullable = false)
    private boolean phoneNumberVerified = false;

}
