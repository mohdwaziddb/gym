package com.backend.rest;

import com.backend.constant.ApiPathConstants;
import com.backend.service.member.MembershipService;
import com.backend.utility.MobileResponseDTOFactory;
import com.backend.utility.SanitizeData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.Membership.BASE)
public class MembershipController {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @PostMapping(ApiPathConstants.Membership.ASSIGN)
    public Object assignMembership(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipService.assignMembership(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Membership.RENEW)
    public Object renewMembership(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipService.renewMembership(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Membership.CANCEL)
    public Object cancelMembership(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipService.cancelMembership(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @GetMapping(ApiPathConstants.Membership.HISTORY)
    public Object getMembershipHistory(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return membershipService.getMembershipHistory(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }
}
