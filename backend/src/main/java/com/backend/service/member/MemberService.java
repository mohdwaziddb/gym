package com.backend.service.member;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface MemberService {

    Object getMemberList(Map<String, Object> param, HttpServletRequest request);

    Object getMemberById(Map<String, Object> param, HttpServletRequest request);

    Object saveMember(Map<String, Object> param, HttpServletRequest request);

    Object archiveMember(Map<String, Object> param, HttpServletRequest request);
}
