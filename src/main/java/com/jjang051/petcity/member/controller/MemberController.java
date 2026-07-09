package com.jjang051.petcity.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MemberController {


    @GetMapping("/member/login")
    public String login() {
        return "member/login";
    }
}