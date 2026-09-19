package com.backend.repo.gym;

import com.backend.entity.gym.GymUserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymUserRepo extends JpaRepository<GymUserModel, Long> {
    Optional<GymUserModel> findByGymCodeAndUsername(String gymCode, String username);
    Optional<GymUserModel> findByGymCodeAndUsernameAndIsActiveTrue(String gymCode, String username);
}
