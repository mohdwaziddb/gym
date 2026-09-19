package com.backend.service.gym;

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
 */
@Service
public class GymDetailsServiceImpl implements GymDetailsService {

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

            // Header based gym_code for isolation within single DB dbgym
            String headerGymCode = request.getHeader("X-Gym-Code");
            if (headerGymCode == null || headerGymCode.trim().isEmpty()) headerGymCode = request.getHeader("x-gym-code");
            if (headerGymCode != null) headerGymCode = headerGymCode.trim().toUpperCase();

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
                if (m == null || Boolean.TRUE.equals(m.getIsdelete())) continue;
                // Isolation: when X-Gym-Code header present and tenant is shared dbgym, filter to that gym_code only
                if (headerGymCode != null && !headerGymCode.isEmpty() && !headerGymCode.equalsIgnoreCase(stringValue(m.getGymCode()))) {
                    continue;
                }
                Map<String, Object> row = new HashMap<>();
                row.put("id", longValue(m.getId()));
                row.put("gym_name", stringValue(m.getGymName()));
                row.put("gym_code", stringValue(m.getGymCode()));
                row.put("owner_name", stringValue(m.getOwnerName()));
                row.put("contact_no", stringValue(m.getContactNo()));
                row.put("email", stringValue(m.getEmail()));
                row.put("address", stringValue(m.getAddress()));
                row.put("city", stringValue(m.getCity()));
                row.put("monthly_fees", m.getMonthlyFees() == null ? null : m.getMonthlyFees().toString());
                row.put("is_active", booleanValue(m.getIsActive()));
                row.put("tenant", stringValue(TenantContextHolder.getTenant()));
                dataArray.add(row);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data_array", dataArray);
            result.put("result_size", dataArray.size());
            result.put("tenant", stringValue(TenantContextHolder.getTenant()));
            result.put("database", stringValue(request.getAttribute("REQUEST_DATABASE_NAME")));
            return result;
        } catch (Exception e) {
            return mobileResponseDTOFactory.failedMessage(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Object saveGym(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            String gymName = stringValue(param.get("gym_name"));
            String gymCode = stringValue(param.get("gym_code"));
            String ownerName = stringValue(param.get("owner_name"));
            String contactNo = stringValue(param.get("contact_no"));
            String email = stringValue(param.get("email"));
            String address = stringValue(param.get("address"));
            String city = stringValue(param.get("city"));
            String feesStr = stringValue(param.get("monthly_fees"));
            Boolean isActive = param.get("is_active") != null ? booleanValue(param.get("is_active")) : true;

            if (gymName.isEmpty()) {
                return mobileResponseDTOFactory.failedMessage("gym_name is mandatory");
            }
            if (gymCode.isEmpty()) {
                gymCode = gymName.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis() % 10000;
            }

            GymDetailsModel model;
            if (id != null) {
                Optional<GymDetailsModel> opt = gymDetailsRepo.findById(id);
                if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Gym not found for id: " + id);
                model = opt.get();
                if (Boolean.TRUE.equals(model.getIsdelete())) return mobileResponseDTOFactory.failedMessage("Cannot update deleted gym");
            } else {
                // check duplicate gym_code
                Optional<GymDetailsModel> existing = gymDetailsRepo.findByGymCode(gymCode);
                if (existing.isPresent() && Boolean.FALSE.equals(existing.get().getIsdelete())) {
                    return mobileResponseDTOFactory.failedMessage("gym_code already exists: " + gymCode);
                }
                model = new GymDetailsModel();
            }

            model.setGymName(gymName);
            model.setGymCode(gymCode);
            model.setOwnerName(ownerName);
            model.setContactNo(contactNo);
            model.setEmail(email);
            model.setAddress(address);
            model.setCity(city);
            if (!feesStr.isEmpty()) {
                try { model.setMonthlyFees(new BigDecimal(feesStr)); } catch (Exception e) { model.setMonthlyFees(BigDecimal.ZERO); }
            }
            model.setIsActive(isActive);
            model.setIsdelete(false);

            gymDetailsRepo.save(model);

            Map<String, Object> data = new HashMap<>();
            data.put("id", model.getId());
            data.put("gym_name", model.getGymName());
            data.put("gym_code", model.getGymCode());
            return mobileResponseDTOFactory.successMessage("Gym saved successfully", data);
        } catch (Exception e) {
            return mobileResponseDTOFactory.failedMessage(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getGymById(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<GymDetailsModel> opt = gymDetailsRepo.findById(id);
            if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getIsdelete())) {
                return mobileResponseDTOFactory.failedMessage("Gym not found");
            }
            GymDetailsModel m = opt.get();
            Map<String, Object> row = new HashMap<>();
            row.put("id", m.getId());
            row.put("gym_name", m.getGymName());
            row.put("gym_code", m.getGymCode());
            row.put("owner_name", m.getOwnerName());
            row.put("contact_no", m.getContactNo());
            row.put("email", m.getEmail());
            row.put("address", m.getAddress());
            row.put("city", m.getCity());
            row.put("monthly_fees", m.getMonthlyFees());
            row.put("is_active", m.getIsActive());
            row.put("created_on", m.getCreatedOn());
            row.put("tenant", stringValue(TenantContextHolder.getTenant()));
            return row;
        } catch (Exception e) {
            return mobileResponseDTOFactory.failedMessage(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Object deleteGym(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<GymDetailsModel> opt = gymDetailsRepo.findById(id);
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Gym not found");
            GymDetailsModel m = opt.get();
            m.setIsdelete(true);
            m.setIsActive(false);
            gymDetailsRepo.save(m);
            return mobileResponseDTOFactory.successMessage("Gym deleted (soft) successfully", null);
        } catch (Exception e) {
            return mobileResponseDTOFactory.failedMessage(e.getMessage());
        }
    }
}
