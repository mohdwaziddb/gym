package com.backend.entity.gym;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GymDetailsModel - Maps to gym_details table (per-tenant DATABASE).
 * Each tenant DB (hsr, gym_lajpat, gym_noida) has its own gym_details rows.
 */
@Entity
@Table(name = "gym_details")
@Getter
@Setter
public class GymDetailsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_name", nullable = false, length = 150)
    private String gymName;

    @Column(name = "gym_code", unique = true, length = 50)
    private String gymCode;

    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @Column(name = "contact_no", length = 15)
    private String contactNo;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "monthly_fees", precision = 10, scale = 2)
    private BigDecimal monthlyFees;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "isdelete")
    private Boolean isdelete = false;

    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    public void prePersist() {
        if (createdOn == null) createdOn = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isdelete == null) isdelete = false;
    }
}
