package com.backend.service.gym;

import com.backend.core.auth.AuthInterceptor;
import com.backend.core.multitenancy.TenantContextHolder;
import com.backend.entity.gym.GymDetailsModel;
import com.backend.repo.gym.GymDetailsRepo;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.backend.utility.DataTypeUtility.*;

/**
 * GymDetailsServiceImpl - Gym management with DATABASE-per-tenant handling.
 * Isolation rule: login wale gym ka code (JWT se) jis record ke gym_code se match kare, sirf wahi
 * list / get / edit / delete ho sakta hai.
 */
@Service
public class GymDetailsServiceImpl implements GymDetailsService {

    private static final String GENERIC_ERROR = "Something went wrong, please try again";

    @Autowired
    private GymDetailsRepo gymDetailsRepo;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @Override
    @Transactional(readOnly = true)
    public Object getGymList(Map<String, Object> param, HttpServletRequest request) {
        try {
            String fts = stringValue(param.get("fts"));
            String city = stringValue(param.get("city"));
            String gymName = stringValue(param.get("gym_name"));
            String currentGymCode = currentGymCode(request);

            List<GymDetailsModel> list;

            if (!fts.isEmpty()) {
                list = gymDetailsRepo.findByGymNameContainingIgnoreCase(fts);
            } else if (!city.isEmpty()) {
                list = gymDetailsRepo.findByCityIgnoreCase(city);
            } else if (!gymName.isEmpty()) {
                list = gymDetailsRepo.findByGymNameContainingIgnoreCase(gymName);
            } else {
                list = gymDetailsRepo.findAllByIsdeleteFalseOrIsdeleteIsNull();
            }

            if (list == null) list = Collections.emptyList();

            List<Map<String, Object>> dataArray = new LinkedList<>();
            for (GymDetailsModel m : list) {
                if (!isAccessible(m, currentGymCode)) continue;
                Map<String, Object> row = new HashMap<>();
                row.put("id", longValue(m.getId()));
                row.put("gym_name", stringValue(m.getGymName()));
                row.put("gym_code", stringValue(m.getGymCode()));
                row.put("owner_name", stringValue(m.getOwnerName()));
                row.put("contact_no", stringValue(m.getContactNo()));
                row.put("email", stringValue(m.getEmail()));
                row.put("address", stringValue(m.getAddress()));
                row.put("city", stringValue(m.getCity()));
                row.put("monthly_fees", m.getMonthlyFees() == null ? null : stringValue(m.getMonthlyFees().toPlainString()));
                row.put("is_active", booleanValue(m.getIsActive()));
                dataArray.add(row);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data_array", dataArray);
            result.put("result_size", dataArray.size());
            return mobileResponseDTOFactory.successMessage("Gym list", result);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object saveGym(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            String gymName = stringValue(param.get("gym_name"));
            String gymCode = stringValue(param.get("gym_code")).toUpperCase();
            String currentGymCode = currentGymCode(request);

            if (gymName.isEmpty()) {
                return mobileResponseDTOFactory.failedMessage("gym_name is mandatory");
            }

            BigDecimal monthlyFees = null;
            String feesStr = stringValue(param.get("monthly_fees"));
            if (!feesStr.isEmpty()) {
                try {
                    monthlyFees = new BigDecimal(feesStr);
                } catch (NumberFormatException e) {
                    return mobileResponseDTOFactory.failedMessage("monthly_fees must be a number");
                }
                if (monthlyFees.signum() < 0) return mobileResponseDTOFactory.failedMessage("monthly_fees cannot be negative");
            }

            GymDetailsModel model;
            if (id != null) {
                Optional<GymDetailsModel> opt = gymDetailsRepo.findById(id);
                if (opt.isEmpty() || !isAccessible(opt.get(), currentGymCode)) {
                    return mobileResponseDTOFactory.failedMessage("Gym not found");
                }
                model = opt.get();
                // gym_code hi login/isolation ki pehchaan hai, edit me badla to record apne hi gym se gayab ho jayega
                if (!gymCode.isEmpty() && !gymCode.equalsIgnoreCase(stringValue(model.getGymCode()))) {
                    return mobileResponseDTOFactory.failedMessage("gym_code cannot be changed");
                }
                gymCode = stringValue(model.getGymCode());
            } else {
                if (gymCode.isEmpty()) {
                    gymCode = gymName.toUpperCase().replaceAll("[^A-Z0-9]", "_") + "_" + System.currentTimeMillis() % 10000;
                }
                model = new GymDetailsModel();
            }

            // duplicate gym_code check (add + edit dono me) - same record ko chhod ke
            Optional<GymDetailsModel> existing = gymDetailsRepo.findByGymCode(gymCode);
            if (existing.isPresent() && !Objects.equals(existing.get().getId(), model.getId())) {
                return mobileResponseDTOFactory.failedMessage("gym_code already exists: " + gymCode);
            }

            model.setGymName(gymName);
            model.setGymCode(gymCode);
            // Edit me sirf wahi fields update hon jo request me aayi hain, baaki purani value bani rahe
            if (id == null || param.containsKey("owner_name")) model.setOwnerName(stringValue(param.get("owner_name")));
            if (id == null || param.containsKey("contact_no")) model.setContactNo(stringValue(param.get("contact_no")));
            if (id == null || param.containsKey("email")) model.setEmail(stringValue(param.get("email")));
            if (id == null || param.containsKey("address")) model.setAddress(stringValue(param.get("address")));
            if (id == null || param.containsKey("city")) model.setCity(stringValue(param.get("city")));
            if (id == null || param.containsKey("monthly_fees")) model.setMonthlyFees(monthlyFees);
            if (param.containsKey("is_active")) model.setIsActive(booleanValue(param.get("is_active")));
            else if (id == null) model.setIsActive(true);
            model.setIsdelete(false);

            gymDetailsRepo.save(model);

            Map<String, Object> data = new HashMap<>();
            data.put("id", model.getId());
            data.put("gym_name", model.getGymName());
            data.put("gym_code", model.getGymCode());
            return mobileResponseDTOFactory.successMessage("Gym saved successfully", data);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getGymById(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<GymDetailsModel> opt = gymDetailsRepo.findById(id);
            if (opt.isEmpty() || !isAccessible(opt.get(), currentGymCode(request))) {
                return mobileResponseDTOFactory.failedMessage("Gym not found");
            }
            GymDetailsModel m = opt.get();
            Map<String, Object> row = new HashMap<>();
            row.put("id", longValue(m.getId()));
            row.put("gym_name", stringValue(m.getGymName()));
            row.put("gym_code", stringValue(m.getGymCode()));
            row.put("owner_name", stringValue(m.getOwnerName()));
            row.put("contact_no", stringValue(m.getContactNo()));
            row.put("email", stringValue(m.getEmail()));
            row.put("address", stringValue(m.getAddress()));
            row.put("city", stringValue(m.getCity()));
            row.put("monthly_fees", m.getMonthlyFees() == null ? null : stringValue(m.getMonthlyFees().toPlainString()));
            row.put("is_active", booleanValue(m.getIsActive()));
            // LocalDateTime ko string me bhejo (plain ObjectMapper date type serialize nahi karta)
            row.put("created_on", stringValue(m.getCreatedOn()));
            return mobileResponseDTOFactory.successMessage("Gym details", row);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object deleteGym(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<GymDetailsModel> opt = gymDetailsRepo.findById(id);
            if (opt.isEmpty() || !isAccessible(opt.get(), currentGymCode(request))) {
                return mobileResponseDTOFactory.failedMessage("Gym not found");
            }
            GymDetailsModel m = opt.get();
            m.setIsdelete(true);
            m.setIsActive(false);
            gymDetailsRepo.save(m);
            return mobileResponseDTOFactory.successMessage("Gym deleted (soft) successfully", null);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    /**
     * Login wale gym ka code - AuthInterceptor JWT verify karke request attribute me rakhta hai.
     */
    private String currentGymCode(HttpServletRequest request) {
        return stringValue(request.getAttribute(AuthInterceptor.AUTH_GYM_CODE)).toUpperCase();
    }

    /**
     * Record deleted na ho aur login wale gym ka hi ho.
     */
    private boolean isAccessible(GymDetailsModel m, String currentGymCode) {
        if (m == null || Boolean.TRUE.equals(m.getIsdelete())) return false;
        return !currentGymCode.isEmpty() && currentGymCode.equalsIgnoreCase(stringValue(m.getGymCode()));
    }
}
