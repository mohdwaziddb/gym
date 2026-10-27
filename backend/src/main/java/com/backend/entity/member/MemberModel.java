package com.backend.entity.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MemberModel - members table. gym_code = login wale gym ka code (isolation).
 */
@Entity
@Table(name = "members")
@Getter
@Setter
public class MemberModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_code", nullable = false, length = 50)
    private String gymCode;

    @Column(name = "member_code", nullable = false, length = 30)
    private String memberCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "emergency_name", length = 100)
    private String emergencyName;

    @Column(name = "emergency_phone", length = 15)
    private String emergencyPhone;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
        if (isdelete == null) isdelete = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
    }
}
