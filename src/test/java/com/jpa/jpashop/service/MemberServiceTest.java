package com.jpa.jpashop.service;

import com.jpa.jpashop.domain.Member;
import com.jpa.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

// ===== 테스트 할건데, 메모리에 올려서 할거야 이때 스프링부트도 활성화? 시작? 되야함(그래야 @autowired 같은것들 활성화 됨)
@RunWith(SpringRunner.class)
@SpringBootTest
// =====
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    @Rollback(false)
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");
        //when
        Long saveId = memberService.join(member);
        //then
        Assert.assertEquals(member, memberRepository.findOne(saveId)); // 한 트랜젝션 안에서 같은 id를 가진 멤버는 같은 멤버이다.
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim2");

        Member member2 = new Member();
        member2.setName("kim2");
        //when
        memberService.join(member1);
        memberService.join(member2); // 예외가 발생해야 함.
        //then
        fail("예외가 발생해야 하는데 안했다.");
    }

}