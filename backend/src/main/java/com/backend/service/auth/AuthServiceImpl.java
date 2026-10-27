package com.backend.service.auth;

import com.backend.core.auth.JwtTokenService;
import com.backend.core.gymcommon.GymRegistryService;
import com.backend.core.multitenancy.TenantContextHolder;
import com.backend.entity.gym.GymUserModel;
import com.backend.repo.gym.GymUserRepo;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Autowired
    private JwtTokenService jwtTokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage("Unable to validate gym code, please try again");
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

            // Switch tenant to that gym's DB (write pool - purana plain password hash me update ho sakta hai)
            TenantContextHolder.setTenantId(dbName);
            Optional<GymUserModel> opt = gymUserRepo.findByGymCodeAndUsername(gymCode, username);
            if (opt.isEmpty() || !passwordMatches(opt.get(), password)) {
                return mobileResponseDTOFactory.failedMessage("Invalid username or password");
            }

            GymUserModel user = opt.get();
            if (!Boolean.TRUE.equals(user.getIsActive())) return mobileResponseDTOFactory.failedMessage("User is inactive");

            String token = jwtTokenService.generateToken(gymCode, username, user.getId(), stringValue(user.getRole()));

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
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage("Login failed, please try again");
        }
    }

    /**
     * BCrypt hash se password match karta hai.
     * Purane users jinka password plain text me save hai: plain match hone pe usi waqt BCrypt hash me convert kar deta hai.
     */
    private boolean passwordMatches(GymUserModel user, String password) {
        String stored = stringValue(user.getPasswordHash());
        if (stored.isEmpty()) return false;
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(password, stored);
        }
        if (!stored.equals(password)) return false;
        user.setPasswordHash(passwordEncoder.encode(password));
        gymUserRepo.save(user);
        return true;
    }
}
