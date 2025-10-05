package com.satwik.splitora.repository;

import com.satwik.splitora.persistence.entities.UserEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserEventsRepository extends JpaRepository<UserEvents, UUID> {
    /* primary purpose is to update the user events,
        otherwise user repository will be used
        to access the user events.
     */
}
