package com.backend.service.member;

import com.backend.entity.member.MembershipPlanModel;
import com.backend.repo.member.MembershipPlanRepo;
import com.backend.utility.AuthContext;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.backend.utility.DataTypeUtility.*;

/**
 * MembershipPlanServiceImpl - Gym ke membership plans. Dekh sab sakte hain, banana / badalna sirf Owner-Admin.
 */
@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private static final String GENERIC_ERROR = "Something went wrong, please try again";
    private static final int MAX_CUSTOM_DAYS = 3650;

    @Autowired
    private MembershipPlanRepo membershipPlanRepo;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @Override
    @Transactional(readOnly = true)
    public Object getPlanList(Map<String, Object> param, HttpServletRequest request) {
        try {
            boolean onlyActive = booleanValue(param.get("only_active"));
            List<Map<String, Object>> dataArray = new LinkedList<>();
            for (MembershipPlanModel p : membershipPlanRepo.findByGymCodeAndIsdeleteFalseOrderByDurationDaysAscPlanNameAsc(AuthContext.gymCode(request))) {
                if (onlyActive && !Boolean.TRUE.equals(p.getIsActive())) continue;
                dataArray.add(planToMap(p));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("data_array", dataArray);
            result.put("result_size", dataArray.size());
            return mobileResponseDTOFactory.successMessage("Plan list", result);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object savePlan(Map<String, Object> param, HttpServletRequest request) {
        try {
            if (!AuthContext.isManager(request)) return mobileResponseDTOFactory.failedMessage("Permission denied");
            String gymCode = AuthContext.gymCode(request);
            Long id = longValue(param.get("id"));
            String planName = stringValue(param.get("plan_name"));
            String durationType = stringValue(param.get("duration_type")).toUpperCase();

            if (planName.isEmpty()) return mobileResponseDTOFactory.failedMessage("plan_name is mandatory");
            if (planName.length() > 100) return mobileResponseDTOFactory.failedMessage("plan_name is too long");
            if (!MembershipDateCalculator.isValidDurationType(durationType)) {
                return mobileResponseDTOFactory.failedMessage("duration_type must be MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY or CUSTOM");
            }

            Integer durationDays = MembershipDateCalculator.defaultDurationDays(durationType);
            if (durationDays == null) {
                durationDays = integerValue(param.get("duration_days"));
                if (durationDays == null || durationDays <= 0 || durationDays > MAX_CUSTOM_DAYS) {
                    return mobileResponseDTOFactory.failedMessage("duration_days must be between 1 and " + MAX_CUSTOM_DAYS);
                }
            }

            BigDecimal price;
            try {
                price = new BigDecimal(stringValue(param.get("price")));
            } catch (NumberFormatException e) {
                return mobileResponseDTOFactory.failedMessage("price must be a number");
            }
            if (price.signum() < 0) return mobileResponseDTOFactory.failedMessage("price cannot be negative");

            MembershipPlanModel model;
            if (id != null) {
                Optional<MembershipPlanModel> opt = membershipPlanRepo.findByIdAndGymCodeAndIsdeleteFalse(id, gymCode);
                if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Plan not found");
                model = opt.get();
            } else {
                model = new MembershipPlanModel();
                model.setGymCode(gymCode);
                model.setCreatedBy(AuthContext.username(request));
            }

            Optional<MembershipPlanModel> sameName = membershipPlanRepo.findFirstByGymCodeAndPlanNameIgnoreCaseAndIsdeleteFalse(gymCode, planName);
            if (sameName.isPresent() && !Objects.equals(sameName.get().getId(), model.getId())) {
                return mobileResponseDTOFactory.failedMessage("Plan already exists: " + planName);
            }

            model.setPlanName(planName);
            model.setDurationType(durationType);
            model.setDurationDays(durationDays);
            model.setPrice(price);
            if (id == null || param.containsKey("description")) model.setDescription(stringValue(param.get("description")));
            if (param.containsKey("is_active")) model.setIsActive(booleanValue(param.get("is_active")));
            model.setUpdatedBy(AuthContext.username(request));
            membershipPlanRepo.save(model);

            return mobileResponseDTOFactory.successMessage("Plan saved successfully", planToMap(model));
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object togglePlan(Map<String, Object> param, HttpServletRequest request) {
        try {
            if (!AuthContext.isManager(request)) return mobileResponseDTOFactory.failedMessage("Permission denied");
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<MembershipPlanModel> opt = membershipPlanRepo.findByIdAndGymCodeAndIsdeleteFalse(id, AuthContext.gymCode(request));
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Plan not found");
            MembershipPlanModel p = opt.get();
            p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
            p.setUpdatedBy(AuthContext.username(request));
            membershipPlanRepo.save(p);
            return mobileResponseDTOFactory.successMessage(Boolean.TRUE.equals(p.getIsActive()) ? "Plan activated" : "Plan deactivated", planToMap(p));
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object deletePlan(Map<String, Object> param, HttpServletRequest request) {
        try {
            if (!AuthContext.isManager(request)) return mobileResponseDTOFactory.failedMessage("Permission denied");
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<MembershipPlanModel> opt = membershipPlanRepo.findByIdAndGymCodeAndIsdeleteFalse(id, AuthContext.gymCode(request));
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Plan not found");
            // Soft delete - purani memberships me plan_name snapshot bacha rehta hai
            MembershipPlanModel p = opt.get();
            p.setIsdelete(true);
            p.setIsActive(false);
            p.setUpdatedBy(AuthContext.username(request));
            membershipPlanRepo.save(p);
            return mobileResponseDTOFactory.successMessage("Plan deleted successfully", null);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    static Map<String, Object> planToMap(MembershipPlanModel p) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", longValue(p.getId()));
        row.put("plan_name", stringValue(p.getPlanName()));
        row.put("duration_type", stringValue(p.getDurationType()));
        row.put("duration_days", integerValue(p.getDurationDays()));
        row.put("price", MembershipServiceImpl.money(p.getPrice()));
        row.put("description", stringValue(p.getDescription()));
        row.put("is_active", booleanValue(p.getIsActive()));
        return row;
    }
}
