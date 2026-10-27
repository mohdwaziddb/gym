package com.backend.rest;

import com.backend.constant.ApiPathConstants;
import com.backend.service.member.MemberService;
import com.backend.utility.MobileResponseDTOFactory;
import com.backend.utility.SanitizeData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.Member.BASE)
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @GetMapping(ApiPathConstants.Member.LIST)
    public Object getMemberList(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return memberService.getMemberList(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @GetMapping(ApiPathConstants.Member.GET_BY_ID)
    public Object getMemberById(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return memberService.getMemberById(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Member.SAVE)
    public Object saveMember(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return memberService.saveMember(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }

    @PostMapping(ApiPathConstants.Member.ARCHIVE)
    public Object archiveMember(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return memberService.archiveMember(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }
}
