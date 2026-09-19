package com.backend.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AuthService {
    Object validateGymCode(Map<String, Object> param, HttpServletRequest request);
    Object login(Map<String, Object> param, HttpServletRequest request);
}
