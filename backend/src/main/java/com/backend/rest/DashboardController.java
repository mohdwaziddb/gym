package com.backend.rest;

import com.backend.constant.ApiPathConstants;
import com.backend.service.dashboard.DashboardService;
import com.backend.utility.MobileResponseDTOFactory;
import com.backend.utility.SanitizeData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.Dashboard.BASE)
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @GetMapping(ApiPathConstants.Dashboard.SUMMARY)
    public Object getSummary(@RequestParam Map<String, Object> param, HttpServletRequest request) {
        try {
            param = SanitizeData.sanitizeMapObj(param);
            return dashboardService.getSummary(param, request);
        } catch (Exception e) {
            return mobileResponseDTOFactory.reportInternalServerError(e);
        }
    }
}
