package com.backend.service.member;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface MembershipPlanService {

    Object getPlanList(Map<String, Object> param, HttpServletRequest request);

    Object savePlan(Map<String, Object> param, HttpServletRequest request);

    Object togglePlan(Map<String, Object> param, HttpServletRequest request);

    Object deletePlan(Map<String, Object> param, HttpServletRequest request);
}
