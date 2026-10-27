package com.backend.repo.member;

import com.backend.entity.member.MembershipModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepo extends JpaRepository<MembershipModel, Long> {

    List<MembershipModel> findByGymCodeAndMemberIdOrderByStartDateDescIdDesc(String gymCode, Long memberId);

    List<MembershipModel> findByGymCodeAndMemberIdAndStatus(String gymCode, Long memberId, String status);

    Optional<MembershipModel> findByIdAndGymCode(Long id, String gymCode);
}
