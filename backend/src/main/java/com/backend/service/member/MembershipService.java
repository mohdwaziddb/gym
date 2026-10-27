package com.backend.service.member;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface MembershipService {

    Object assignMembership(Map<String, Object> param, HttpServletRequest request);

    Object renewMembership(Map<String, Object> param, HttpServletRequest request);

    Object cancelMembership(Map<String, Object> param, HttpServletRequest request);

    Object getMembershipHistory(Map<String, Object> param, HttpServletRequest request);
}
