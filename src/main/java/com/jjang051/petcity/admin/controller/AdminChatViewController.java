package com.jjang051.petcity.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatViewController {


    @GetMapping()
    public String chatManagement(@RequestParam(name = "view", defaultValue = "all") String view,
                                 @RequestParam(name = "roomUuid", required = false) String roomUuid,
                                 Model model) {
        String normalizedView = normalizeView(view);
        model.addAttribute("chatView", normalizedView);
        model.addAttribute("selectedRoomUuid", roomUuid);
        return "admin/chat-management";
    }

    private String normalizeView(String view) {
        if ("unread".equalsIgnoreCase(view)) {
            return "unread";
        }
        return "all";

    }

}
