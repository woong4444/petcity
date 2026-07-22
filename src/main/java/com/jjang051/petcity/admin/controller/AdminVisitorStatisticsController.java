package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminVisitorStatisticsDto;
import com.jjang051.petcity.admin.dto.DailyVisitorStatisticsDto;
import com.jjang051.petcity.admin.service.AdminVisitorStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminVisitorStatisticsController {

    private final AdminVisitorStatisticsService adminVisitorStatisticsService;

    @GetMapping("/visitor-statistics")
    public String visitorStatistics(Model model) {
        AdminVisitorStatisticsDto statistics = adminVisitorStatisticsService.getVisitorStatistics();
        List<DailyVisitorStatisticsDto> chartStatistics = new ArrayList<>(statistics.getDailyStatistics());
        Collections.reverse(chartStatistics);
        List<String> chartLabels = chartStatistics.stream().map(DailyVisitorStatisticsDto::getVisitDate)
                .map(visitDate -> visitDate.substring(5))
                .toList();

        List<Long> chartLoginData = chartStatistics.stream().map(DailyVisitorStatisticsDto::getLoginVisitorCount)
                .toList();
        List<Long> chartGuestData = chartStatistics.stream().map(DailyVisitorStatisticsDto::getGuestVisitorCount)
                .toList();

        model.addAttribute("statistics", statistics);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartLoginData", chartLoginData);
        model.addAttribute("chartGuestData", chartGuestData);

        return "admin/visitor-statistics";
    }


}
