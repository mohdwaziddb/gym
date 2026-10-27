package com.backend.service.member;

import com.backend.entity.member.MemberModel;
import com.backend.entity.member.MembershipModel;
import com.backend.entity.member.MembershipPlanModel;
import com.backend.repo.member.MemberRepo;
import com.backend.repo.member.MembershipPlanRepo;
import com.backend.repo.member.MembershipRepo;
import com.backend.utility.AuthContext;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.backend.utility.DataTypeUtility.*;

/**
 * MembershipServiceImpl - Member ko plan assign / renew / cancel.
 * Assign aur payment alag hain: yaha bani membership UNPAID rehti hai (payments Phase 2).
 */
@Service
public class MembershipServiceImpl implements MembershipService {

    private static final String GENERIC_ERROR = "Something went wrong, please try again";

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private MembershipPlanRepo membershipPlanRepo;

    @Autowired
    private MembershipRepo membershipRepo;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @Override
    @Transactional
    public Object assignMembership(Map<String, Object> param, HttpServletRequest request) {
        try {
            LocalDate start = MembershipDateCalculator.parseDate(param.get("start_date"));
            if (!stringValue(param.get("start_date")).isEmpty() && start == null) {
                return mobileResponseDTOFactory.failedMessage("start_date must be YYYY-MM-DD");
            }
            return createMembership(param, request, start, false);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object renewMembership(Map<String, Object> param, HttpServletRequest request) {
        try {
            // Renew hamesha chalu membership ke baad se (ya aaj se agar khatam ho chuki)
            return createMembership(param, request, null, true);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    /**
     * Assign + Renew ka common flow. Member row lock hoti hai, isliye double tap / do staff ek saath
     * renew karein to bhi overlap check ke baad doosri request reject ho jaati hai.
     */
    private Object createMembership(Map<String, Object> param, HttpServletRequest request, LocalDate requestedStart, boolean renew) {
        String gymCode = AuthContext.gymCode(request);
        Long memberId = longValue(param.get("member_id"));
        Long planId = longValue(param.get("plan_id"));
        if (memberId == null) return mobileResponseDTOFactory.failedMessage("member_id is required");

        Optional<MemberModel> member = memberRepo.lockByIdAndGymCode(memberId, gymCode);
        if (member.isEmpty()) return mobileResponseDTOFactory.failedMessage("Member not found");

        List<MembershipModel> active = membershipRepo.findByGymCodeAndMemberIdAndStatus(gymCode, memberId, MembershipModel.STATUS_ACTIVE);
        MembershipModel latest = null;
        for (MembershipModel ms : active) {
            if (latest == null || ms.getEndDate().isAfter(latest.getEndDate())) latest = ms;
        }

        // Renew me plan na diya ho to pichla plan hi
        if (planId == null && renew && latest != null) planId = latest.getPlanId();
        if (planId == null) return mobileResponseDTOFactory.failedMessage("plan_id is required");

        Optional<MembershipPlanModel> planOpt = membershipPlanRepo.findByIdAndGymCodeAndIsdeleteFalse(planId, gymCode);
        if (planOpt.isEmpty() || !Boolean.TRUE.equals(planOpt.get().getIsActive())) {
            return mobileResponseDTOFactory.failedMessage("Plan not found or inactive");
        }
        MembershipPlanModel plan = planOpt.get();

        BigDecimal discount = BigDecimal.ZERO;
        String discountStr = stringValue(param.get("discount"));
        if (!discountStr.isEmpty()) {
            try {
                discount = new BigDecimal(discountStr);
            } catch (NumberFormatException e) {
                return mobileResponseDTOFactory.failedMessage("discount must be a number");
            }
        }
        if (discount.signum() < 0) return mobileResponseDTOFactory.failedMessage("discount cannot be negative");
        if (discount.compareTo(plan.getPrice()) > 0) return mobileResponseDTOFactory.failedMessage("discount cannot be more than plan price");

        // Renew double tap: app wahi membership id bhejta hai jise renew kar raha hai.
        // Beech me koi aur renew ho chuka ho to latest badal jaata hai -> reject (dobara membership nahi banegi)
        Long afterMembershipId = longValue(param.get("after_membership_id"));
        if (renew && afterMembershipId != null && (latest == null || !afterMembershipId.equals(latest.getId()))) {
            return mobileResponseDTOFactory.failedMessage("Membership was already renewed. Please refresh.");
        }

        // Assign: start na diya ho to aaj se. Renew: chalu membership ke baad se (ya aaj se agar khatam).
        LocalDate today = LocalDate.now();
        LocalDate start;
        if (renew) start = MembershipDateCalculator.renewalStartDate(latest == null ? null : latest.getEndDate(), today);
        else start = requestedStart != null ? requestedStart : today;
        LocalDate end = MembershipDateCalculator.endDate(start, plan.getDurationDays());

        for (MembershipModel ms : active) {
            if (MembershipDateCalculator.overlaps(start, end, ms.getStartDate(), ms.getEndDate())) {
                return mobileResponseDTOFactory.failedMessage("Member already has a membership from "
                        + ms.getStartDate() + " to " + ms.getEndDate() + ". Use renew or choose a later start date.");
            }
        }

        MembershipModel ms = new MembershipModel();
        ms.setGymCode(gymCode);
        ms.setMemberId(memberId);
        ms.setPlanId(plan.getId());
        ms.setPlanName(plan.getPlanName());
        ms.setStartDate(start);
        ms.setEndDate(end);
        ms.setPrice(plan.getPrice());
        ms.setDiscount(discount);
        ms.setFinalAmount(plan.getPrice().subtract(discount));
        ms.setStatus(MembershipModel.STATUS_ACTIVE);
        ms.setPaymentStatus(MembershipModel.PAYMENT_UNPAID);
        if (renew && latest != null) ms.setRenewedFromId(latest.getId());
        ms.setCreatedBy(AuthContext.username(request));
        ms.setUpdatedBy(AuthContext.username(request));
        membershipRepo.save(ms);

        return mobileResponseDTOFactory.successMessage(renew ? "Membership renewed successfully" : "Membership assigned successfully", membershipToMap(ms, today));
    }

    @Override
    @Transactional
    public Object cancelMembership(Map<String, Object> param, HttpServletRequest request) {
        try {
            if (!AuthContext.isManager(request)) return mobileResponseDTOFactory.failedMessage("Permission denied");
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<MembershipModel> opt = membershipRepo.findByIdAndGymCode(id, AuthContext.gymCode(request));
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Membership not found");
            MembershipModel ms = opt.get();
            if (!MembershipModel.STATUS_ACTIVE.equals(ms.getStatus())) return mobileResponseDTOFactory.failedMessage("Membership is already cancelled");
            ms.setStatus(MembershipModel.STATUS_CANCELLED);
            ms.setUpdatedBy(AuthContext.username(request));
            membershipRepo.save(ms);
            return mobileResponseDTOFactory.successMessage("Membership cancelled", null);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMembershipHistory(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long memberId = longValue(param.get("member_id"));
            if (memberId == null) return mobileResponseDTOFactory.failedMessage("member_id is required");
            String gymCode = AuthContext.gymCode(request);
            if (memberRepo.findByIdAndGymCodeAndIsdeleteFalse(memberId, gymCode).isEmpty()) {
                return mobileResponseDTOFactory.failedMessage("Member not found");
            }
            return mobileResponseDTOFactory.successMessage("Membership history",
                    membershipSummary(membershipRepo.findByGymCodeAndMemberIdOrderByStartDateDescIdDesc(gymCode, memberId), LocalDate.now()));
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    /**
     * Member detail / history ke liye: status, chalu membership, aage wali, renew start date aur poori history.
     * list newest-first honi chahiye.
     */
    static Map<String, Object> membershipSummary(List<MembershipModel> list, LocalDate today) {
        List<LocalDate[]> activePeriods = new ArrayList<>();
        MembershipModel current = null;
        MembershipModel upcoming = null;
        LocalDate latestEnd = null;
        List<Map<String, Object>> history = new LinkedList<>();

        for (MembershipModel ms : list) {
            history.add(membershipToMap(ms, today));
            if (!MembershipModel.STATUS_ACTIVE.equals(ms.getStatus())) continue;
            activePeriods.add(new LocalDate[]{ms.getStartDate(), ms.getEndDate()});
            if (latestEnd == null || ms.getEndDate().isAfter(latestEnd)) latestEnd = ms.getEndDate();
            boolean covering = !ms.getStartDate().isAfter(today) && !ms.getEndDate().isBefore(today);
            if (covering) current = ms;
            else if (ms.getStartDate().isAfter(today) && (upcoming == null || ms.getStartDate().isBefore(upcoming.getStartDate()))) upcoming = ms;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("status", MembershipDateCalculator.memberStatus(activePeriods, today));
        data.put("current_membership", current == null ? null : membershipToMap(current, today));
        data.put("upcoming_membership", upcoming == null ? null : membershipToMap(upcoming, today));
        data.put("expiry_date", latestEnd == null ? "" : stringValue(latestEnd));
        data.put("renewal_start_date", stringValue(MembershipDateCalculator.renewalStartDate(latestEnd, today)));
        // Renew request me after_membership_id isi ko bhejna hai
        data.put("latest_membership_id", latestId(list));
        data.put("memberships", history);
        return data;
    }

    static Map<String, Object> membershipToMap(MembershipModel ms, LocalDate today) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", longValue(ms.getId()));
        row.put("plan_id", longValue(ms.getPlanId()));
        row.put("plan_name", stringValue(ms.getPlanName()));
        row.put("start_date", stringValue(ms.getStartDate()));
        row.put("end_date", stringValue(ms.getEndDate()));
        row.put("price", money(ms.getPrice()));
        row.put("discount", money(ms.getDiscount()));
        row.put("final_amount", money(ms.getFinalAmount()));
        row.put("status", stringValue(ms.getStatus()));
        row.put("payment_status", stringValue(ms.getPaymentStatus()));
        row.put("renewed_from_id", longValue(ms.getRenewedFromId()));
        long daysLeft = ChronoUnit.DAYS.between(today, ms.getEndDate());
        row.put("days_left", Math.max(daysLeft, 0));
        row.put("created_on", stringValue(ms.getCreatedOn()));
        return row;
    }

    /** Sabse aage tak chalne wali ACTIVE membership ki id (renew isi ke baad lagta hai) */
    private static Long latestId(List<MembershipModel> list) {
        MembershipModel latest = null;
        for (MembershipModel ms : list) {
            if (!MembershipModel.STATUS_ACTIVE.equals(ms.getStatus())) continue;
            if (latest == null || ms.getEndDate().isAfter(latest.getEndDate())) latest = ms;
        }
        return latest == null ? null : latest.getId();
    }

    /** 1500 -> "1500.00" (save ke turant baad aur DB se padhne pe ek jaisa) */
    static String money(BigDecimal value) {
        return stringValue((value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
