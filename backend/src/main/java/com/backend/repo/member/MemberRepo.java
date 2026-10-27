package com.backend.repo.member;

import com.backend.entity.member.MemberModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepo extends JpaRepository<MemberModel, Long> {

    Optional<MemberModel> findByIdAndGymCodeAndIsdeleteFalse(Long id, String gymCode);

    Optional<MemberModel> findFirstByGymCodeAndPhoneAndIsdeleteFalse(String gymCode, String phone);

    // Assign / renew ke waqt member row lock - ek saath do renew na ho
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberModel m WHERE m.id = :id AND m.gymCode = :gymCode AND m.isdelete = false")
    Optional<MemberModel> lockByIdAndGymCode(@Param("id") Long id, @Param("gymCode") String gymCode);
}
