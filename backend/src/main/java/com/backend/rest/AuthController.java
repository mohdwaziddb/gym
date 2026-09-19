package com.backend.rest;

import com.backend.constant.ApiPathConstants;
import com.backend.service.auth.AuthService;
import com.backend.utility.MobileResponseDTOFactory;
import com.backend.utility.SanitizeData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.Auth.BASE)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @GetMapping(ApiPathConstants.Auth.VALIDATE_GYM)
    public Object validateGym(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return authService.validateGymCode(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Auth.VALIDATE_GYM)
    public Object validateGymPost(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return authService.validateGymCode(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Auth.LOGIN)
    public Object login(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return authService.login(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(value = "/loginJson", consumes = "application/json")
    public Object loginJson(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return authService.login(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }
}
