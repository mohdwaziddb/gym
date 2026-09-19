package com.backend.service.gym;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface GymDetailsService {

    Object getGymList(Map<String, Object> param, HttpServletRequest request);

    Object saveGym(Map<String, Object> param, HttpServletRequest request);

    Object getGymById(Map<String, Object> param, HttpServletRequest request);

    Object deleteGym(Map<String, Object> param, HttpServletRequest request);
}
