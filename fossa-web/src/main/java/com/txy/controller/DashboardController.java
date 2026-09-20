package com.txy.controller;

import com.txy.vo.DashboardVO;
import com.txy.security.RequirePerm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.txy.dto.R;
import com.txy.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @RequirePerm("dashboard:view")
    @GetMapping("/stats")
    public R<DashboardVO> stats() {
        return R.ok(dashboardService.stats());
    }
}
