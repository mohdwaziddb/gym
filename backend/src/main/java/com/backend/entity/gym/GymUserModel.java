package com.backend.entity.gym;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_user", uniqueConstraints = @UniqueConstraint(columnNames = {"gym_code", "username"}))
@Getter
@Setter
public class GymUserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_code", nullable = false, length = 50)
    private String gymCode;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", length = 20)
    private String role = "ADMIN";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    public void prePersist() {
        if (createdOn == null) createdOn = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }
}
