package com.backend.service.dashboard;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface DashboardService {

    Object getSummary(Map<String, Object> param, HttpServletRequest request);
}
