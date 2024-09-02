package com.example.pizzapaybackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
public class UserSessionEntity {

    /**
     * Unique id of the session
     * Used as bearer
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private UUID id;
    
    /**
     * ID of the user
     */
    private UUID userId;

    /**
     * Full user data
     */
    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserEntity user;

}
