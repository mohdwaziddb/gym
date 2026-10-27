package com.backend.utility;

import com.backend.core.auth.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;

import static com.backend.utility.DataTypeUtility.*;

/**
 * AuthContext - Login wale user ki info (AuthInterceptor JWT se request attributes me rakhta hai).
 * Service me gym_code / role hamesha yahi se lo, client ke bheje param se kabhi nahi.
 */
public final class AuthContext {

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STAFF = "STAFF";

    private AuthContext() {}

    public static String gymCode(HttpServletRequest request) {
        return stringValue(request.getAttribute(AuthInterceptor.AUTH_GYM_CODE)).toUpperCase();
    }

    public static String username(HttpServletRequest request) {
        return stringValue(request.getAttribute(AuthInterceptor.AUTH_USERNAME));
    }

    public static String role(HttpServletRequest request) {
        return stringValue(request.getAttribute(AuthInterceptor.AUTH_ROLE)).toUpperCase();
    }

    public static boolean hasRole(HttpServletRequest request, String... roles) {
        String role = role(request);
        for (String r : roles) {
            if (r.equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    /** Owner / Admin - plans, archive, cancel jaise kaam */
    public static boolean isManager(HttpServletRequest request) {
        return hasRole(request, ROLE_OWNER, ROLE_ADMIN);
    }
}
