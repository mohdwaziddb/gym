package com.backend.rest;

import com.backend.constant.ApiPathConstants;
import com.backend.service.member.MembershipPlanService;
import com.backend.utility.MobileResponseDTOFactory;
import com.backend.utility.SanitizeData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.Plan.BASE)
public class MembershipPlanController {

    @Autowired
    private MembershipPlanService membershipPlanService;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @GetMapping(ApiPathConstants.Plan.LIST)
    public Object getPlanList(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipPlanService.getPlanList(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Plan.SAVE)
    public Object savePlan(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipPlanService.savePlan(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Plan.TOGGLE)
    public Object togglePlan(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipPlanService.togglePlan(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Plan.DELETE)
    public Object deletePlan(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipPlanService.deletePlan(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }
}
