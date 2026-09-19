package com.backend.service.auth;

import com.backend.core.gymcommon.GymRegistryService;
import com.backend.core.multitenancy.TenantContextHolder;
import com.backend.entity.gym.GymUserModel;
import com.backend.repo.gym.GymUserRepo;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.backend.utility.DataTypeUtility.*;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private GymRegistryService gymRegistryService;

    @Autowired
    private GymUserRepo gymUserRepo;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @Value("${jwt.secret:changeme-backend-secret}")
    private String jwtSecret;

    @Override
    public Object validateGymCode(Map<String, Object> param, HttpServletRequest request) {
        try {
            String gymCode = stringValue(param.get("gym_code"));
            if (gymCode.isEmpty()) gymCode = stringValue(param.get("gymCode"));
            if (gymCode.isEmpty()) return mobileResponseDTOFactory.failedMessage("gym_code is required");

            gymCode = gymCode.trim().toUpperCase();
            String dbName = gymRegistryService.resolveDbName(gymCode);
            if (dbName == null) return mobileResponseDTOFactory.failedMessage("Invalid gym code: " + gymCode);

            Map<String, Object> info = gymRegistryService.getGymInfo(gymCode);
            Map<String, Object> data = new HashMap<>();
            data.put("gym_code", gymCode);
            data.put("db_name", dbName);
            data.put("valid", true);
            if (info != null) {
                data.put("gym_name", info.get("gym_name"));
                data.put("status", info.get("status"));
            }
            // also set tenant for this request to verify DB connection
            TenantContextHolder.setTenantId(dbName);
            try {
                // connection cut will happen in afterCompletion
                return mobileResponseDTOFactory.successMessage("Gym code valid", data);
            } finally {
                // keep tenant for this request, clear after
            }
        } catch (Exception e) {
            return mobileResponseDTOFactory.failedMessage(e.getMessage());
        }
    }

    @Override
    public Object login(Map<String, Object> param, HttpServletRequest request) {
        try {
            String gymCode = stringValue(param.get("gym_code"));
            if (gymCode.isEmpty()) gymCode = stringValue(param.get("gymCode"));
            String username = stringValue(param.get("username"));
            String password = stringValue(param.get("password"));

            if (gymCode.isEmpty()) return mobileResponseDTOFactory.failedMessage("gym_code is required");
            if (username.isEmpty() || password.isEmpty()) return mobileResponseDTOFactory.failedMessage("username and password required");

            gymCode = gymCode.trim().toUpperCase();
            String dbName = gymRegistryService.resolveDbName(gymCode);
            if (dbName == null) return mobileResponseDTOFactory.failedMessage("Invalid gym code");

            // Switch tenant to that gym's DB for user lookup
            TenantContextHolder.setTenantId(dbName);
            // For _read vs _write, use write for login audit? Use base
            Optional<GymUserModel> opt = gymUserRepo.findByGymCodeAndUsernameAndIsActiveTrue(gymCode, username);
            // fallback case insensitive
            if (opt.isEmpty()) opt = gymUserRepo.findByGymCodeAndUsername(gymCode, username);
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Invalid username or gym code");

            GymUserModel user = opt.get();
            if (!Boolean.TRUE.equals(user.getIsActive())) return mobileResponseDTOFactory.failedMessage("User is inactive");

            // Simple password check (plain for demo, in production use BCrypt)
            if (!password.equals(user.getPasswordHash())) {
                // try case: if stored as plain, also accept
                return mobileResponseDTOFactory.failedMessage("Invalid password");
            }

            // Generate simple token (Base64 of gym_code:username:timestamp:secret)
            String payload = gymCode + ":" + username + ":" + System.currentTimeMillis() + ":" + jwtSecret;
            String token = Base64.getEncoder().encodeToString(payload.getBytes());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("gym_code", gymCode);
            data.put("db_name", dbName);
            data.put("username", username);
            data.put("role", user.getRole());
            data.put("user_id", user.getId());
            // request attribute for tenant (also set via interceptor next calls)
            request.setAttribute("REQUEST_DATABASE_NAME", dbName);
            return mobileResponseDTOFactory.successMessage("Login successful", data);
        } catch (Exception e) {
            return mobileResponseDTOFactory.failedMessage(e.getMessage());
        }
    }
}
