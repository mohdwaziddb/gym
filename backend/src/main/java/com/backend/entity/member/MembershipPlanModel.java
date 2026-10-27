package com.backend.entity.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MembershipPlanModel - membership_plans table (gym ke apne plans: Monthly, Quarterly ...).
 */
@Entity
@Table(name = "membership_plans")
@Getter
@Setter
public class MembershipPlanModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_code", nullable = false, length = 50)
    private String gymCode;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "duration_type", nullable = false, length = 20)
    private String durationType;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "isdelete")
    private Boolean isdelete = false;

    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @PrePersist
    public void prePersist() {
        if (createdOn == null) createdOn = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isdelete == null) isdelete = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
    }
}
