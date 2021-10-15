package com.shop.service;

import com.shop.dto.JoinFormDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Member createMember(){
        JoinFormDto memberFormDto = new JoinFormDto();
        memberFormDto.setEmail("test@email.com");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void JoinTest() {

        //given
        Member member = createMember();
        memberService.saveMember(member);

        //when
        Member savedMember = memberRepository.findByEmail(member.getEmail());

        //then
        assertEquals(savedMember.getEmail(), member.getEmail());
        assertEquals(savedMember.getName(), member.getName());

    }

    @Test
    @DisplayName("중복회원 테스트")
    public void duplicateMemberTest() {

        //given
        Member member1 = createMember();
        Member member2 = createMember();
        memberService.saveMember(member1);

        //when
        Throwable e = assertThrows(IllegalStateException.class, ()->{
            memberService.saveMember(member2);
        });

        //then
        assertEquals("이미 가입된 회원입니다.", e.getMessage());

    }
}