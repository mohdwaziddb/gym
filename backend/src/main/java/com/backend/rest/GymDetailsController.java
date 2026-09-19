package com.backend.rest;

import com.backend.constant.ApiPathConstants;
import com.backend.service.gym.GymDetailsService;
import com.backend.utility.MobileResponseDTOFactory;
import com.backend.utility.SanitizeData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.Gym.BASE)
public class GymDetailsController {

    @Autowired
    private GymDetailsService gymDetailsService;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @GetMapping(ApiPathConstants.Gym.LIST)
    public Object getGymList(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return gymDetailsService.getGymList(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Gym.SAVE)
    public Object saveGym(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return gymDetailsService.saveGym(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    // JSON body support - POST /rest/gym/saveJson
    @PostMapping(value = "/saveJson", consumes = "application/json")
    public Object saveGymJson(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return gymDetailsService.saveGym(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @GetMapping(ApiPathConstants.Gym.GET_BY_ID)
    public Object getGymById(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return gymDetailsService.getGymById(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Gym.DELETE)
    public Object deleteGym(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return gymDetailsService.deleteGym(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }
}
