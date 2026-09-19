package com.backend.repo.gym;

import com.backend.entity.gym.GymDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymDetailsRepo extends JpaRepository<GymDetailsModel, Long> {

    List<GymDetailsModel> findAllByIsdeleteFalseOrIsdeleteIsNull();

    Optional<GymDetailsModel> findByGymCode(String gymCode);

    List<GymDetailsModel> findByCityIgnoreCase(String city);

    List<GymDetailsModel> findByGymNameContainingIgnoreCase(String name);
}
