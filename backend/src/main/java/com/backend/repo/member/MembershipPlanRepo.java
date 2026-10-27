package com.backend.repo.member;

import com.backend.entity.member.MembershipPlanModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepo extends JpaRepository<MembershipPlanModel, Long> {

    List<MembershipPlanModel> findByGymCodeAndIsdeleteFalseOrderByDurationDaysAscPlanNameAsc(String gymCode);

    Optional<MembershipPlanModel> findByIdAndGymCodeAndIsdeleteFalse(Long id, String gymCode);

    Optional<MembershipPlanModel> findFirstByGymCodeAndPlanNameIgnoreCaseAndIsdeleteFalse(String gymCode, String planName);
}
