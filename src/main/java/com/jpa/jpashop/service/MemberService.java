package com.jpa.jpashop.service;

import com.jpa.jpashop.domain.Member;
import com.jpa.jpashop.domain.Order;
import com.jpa.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에 @Transactional 걸면 public 메소드에 모두 트랜젝션 걸림
                                // readOnly 속성 단순 읽기 작업에 걸어줄 수 있다.  최적화 해줌.
public class MemberService {

    private final MemberRepository memberRepository;
    private final EntityManagerFactory emf;

    /**
     * 회원 등록
     */
    @Transactional
    public Long join(Member member) {
        ValidateDuplicateMember(member); // 중복 회원, 만약 같은 이름의 회원 2명이 동시에 이 코드를 통과하면 익셉션이 발생하지 않고 저장이 되기 때문에
                                        // 서버에서는 최후에 수단으로 디비에 유니크까지 걸어놔야 한다.
        memberRepository.save(member);
        return member.getId();
    }
    private void ValidateDuplicateMember(Member member) {
        // Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        List<Member> all = memberRepository.findAll(); // 지연로딩 세팅으로 orders는 가져오지 않는다.
        System.out.println("========" + all.get(0).getOrders()); // orders 가져옴, (orderItems, delivery도 가져옴.... <- 얘네는 왜 가져오지? 얘네도 지연로딩인데..)
                                                                    // 근데 delivery jackson 매핑될 때 프록시 객체라면서 에러남.
                                                                    // 그럼 프록시 객체라는 소리? 그러탐 왜 select로 delivery를 가져온 것인가.
        return all;
    }

    // 회원 상세 조회
    public Member findMember(Long memberId) {
        Member member = memberRepository.findOne(memberId); // 지연로딩으로 orders는 가져오지 않음.
        System.out.println("=======orders 호출 전" + emf.getPersistenceUnitUtil().isLoaded(member.getOrders())); // orders 안가져옴.
        System.out.println("=======orders 호출 전" + emf.getPersistenceUnitUtil().isLoaded(member.getOrders().get(0).getDelivery()));  // 이 시점에 orders만 가져옴
        System.out.println("=======orders 호출 전" + emf.getPersistenceUnitUtil().isLoaded(member.getOrders().get(0).getOrderItems()));
        System.out.println("=======" + member.getOrders()); // 이 시점에 orderItem, delivery 가져옴.. 왜??? 지연로딩 이라면!! // 근데 orders 0번째 인덱스의 orderItems랑 delivery만 가져옴
        System.out.println("=======orders 호출 후" + emf.getPersistenceUnitUtil().isLoaded(member.getOrders()));
        System.out.println("=======orders 호출 후" + emf.getPersistenceUnitUtil().isLoaded(member.getOrders().get(0).getDelivery())); // true 네....
        System.out.println("=======orders 호출 후" + emf.getPersistenceUnitUtil().isLoaded(member.getOrders().get(0).getOrderItems()));
        System.out.println(member.getOrders().get(1)); // 이 시점에 orders 1번째 인덱스의 orderItems랑 delivery 가져옴.

        // 내 멋대로 정리해본다.
        // 지연로딩된 놈들은 하이버네이트 판단하에 필요한 시점에 로딩된다.
        // 내 생각으로는 member.getOrders()를 출력하는 시점에 delivery와 orderItem이 필요한거 같진 않은데
        // 하이버네이트는 필요하다 생각했다보다 그래서 굳이 이 시점에 delivery와 orderItem 쿼리를 날려 data를 가지고 왔다.
        // 그래도 양심은 있어서 orders의 0번째 인덱스에 대한 orderItems와 delivery만 가져옴.
        // member.getOrders().get(1)을 호출하는 순간 orders의 1번째 인덱스에 대한 orderItems와 delivery를 가져온다.
        // 결론. order는 delivery와 orderItems에 대해 지연로딩 했다.!
        return member;
    }


    @Transactional
    public void updateMember(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
