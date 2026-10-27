package com.backend.core.auth;

import com.backend.utility.GeneralResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.backend.utility.DataTypeUtility.*;

/**
 * AuthInterceptor - Protected APIs (WebConfig me registered paths) pe JWT check.
 * - Authorization: Bearer <token> zaroori
 * - Token ka gym_code aur X-Gym-Code header same hona chahiye (ek gym ka token doosre gym pe nahi chalega)
 * Pass hone pe request attributes AUTH_GYM_CODE / AUTH_USERNAME set hote hain, services inhi ko use karein.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_GYM_CODE = "AUTH_GYM_CODE";
    public static final String AUTH_USERNAME = "AUTH_USERNAME";
    public static final String AUTH_ROLE = "AUTH_ROLE";
    public static final String AUTH_USER_ID = "AUTH_USER_ID";

    @Autowired
    private JwtTokenService jwtTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS preflight me token nahi aata
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        String authHeader = stringValue(request.getHeader("Authorization"));
        if (!authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return unauthorized(response, "Login required");
        }

        Claims claims = jwtTokenService.parseToken(authHeader.substring(7).trim());
        if (claims == null) {
            return unauthorized(response, "Session expired, please login again");
        }

        String tokenGymCode = stringValue(claims.get(JwtTokenService.CLAIM_GYM_CODE)).toUpperCase();
        String headerGymCode = stringValue(request.getHeader("X-Gym-Code")).toUpperCase();
        if (tokenGymCode.isEmpty() || !tokenGymCode.equals(headerGymCode)) {
            return unauthorized(response, "Gym code mismatch, please login again");
        }

        request.setAttribute(AUTH_GYM_CODE, tokenGymCode);
        request.setAttribute(AUTH_USERNAME, stringValue(claims.getSubject()));
        request.setAttribute(AUTH_ROLE, stringValue(claims.get(JwtTokenService.CLAIM_ROLE)).toUpperCase());
        request.setAttribute(AUTH_USER_ID, longValue(claims.get(JwtTokenService.CLAIM_USER_ID)));
        return true;
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new GeneralResponse<>(false, message, null)));
        return false;
    }
}
