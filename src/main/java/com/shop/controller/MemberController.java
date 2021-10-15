package com.shop.controller;


import com.shop.dto.JoinFormDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;

    // 회원가입 페이지
    @GetMapping("/new")
    public String memberJoinForm(JoinFormDto joinFormDto, Model model) {
        model.addAttribute("joinFormDto", joinFormDto);
        return "member/joinForm";
    }

    // 회원가입
    @PostMapping("/new")
    public String memberJoin(@Valid JoinFormDto joinFormDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "member/joinForm";
        }

        try {
            Member member = Member.createMember(joinFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/joinForm";
        }
        return "redirect:/";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String memberLogin() {
        return "member/loginForm";
    }

    // 로그인 실패
    @GetMapping("/login/fail")
    public String memberLoginFail(Model model) {
        model.addAttribute("loginFailMsg", "아이디 또는 비밀번호를 확인해주세요.");
        return "member/loginForm";
    }
}
