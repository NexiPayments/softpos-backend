package com.example.pizzapaybackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
public class UserEntity {

    /**
     * Unique ID for each user
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private UUID id;

    /**
     * User email of the user
     */
    @Column(unique = true)
    private String email;

    /**
     * Point of sales assigned to the user
     */
    private String pointOfSale;

    /**
     * Terminal id assigned to the user for softpos
     */
    private String terminalIdSoftpos;

    /**
     * Terminal id assigned to the user for mpos
     */
    private String terminalIdMpos;

}
