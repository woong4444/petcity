package com.jjang051.petcity.owner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OwnerMainController {

    // "/owner" 주소로 접속하면 자동으로 병원 정보 수정 페이지로 넘겨줍니다.
    @GetMapping("/owner")
    public String ownerMain() {
        return "redirect:/owner/hospital/update";
    }
}