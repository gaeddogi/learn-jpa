package com.jpa.jpashop.service;

import com.jpa.jpashop.domain.Delivery;
import com.jpa.jpashop.domain.Member;
import com.jpa.jpashop.domain.Order;
import com.jpa.jpashop.domain.OrderItem;
import com.jpa.jpashop.domain.item.Item;
import com.jpa.jpashop.repository.ItemRepository;
import com.jpa.jpashop.repository.MemberRepository;
import com.jpa.jpashop.repository.order.OrderRepository;
import com.jpa.jpashop.repository.order.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member findMember = memberRepository.findOne(memberId);
        Item findItem = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(findMember.getAddress());

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(findItem, findItem.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(findMember, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order); // orderItem과 delivery에 cascade를 세팅해주었기 때문에 소유자 영속시 피소유자도 영속됨.
        return order.getId();
    }

    /**
     * 취소
     */
    @Transactional
    public void cacelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order findOrder = orderRepository.findOne(orderId);
        // 주문 취소
        findOrder.cancel();// 이 부분이 이해가 안간다.. orderItems는 mapped속성이라 변경감지 안되는데.
                            // orderItems에 cascade속성 걸려서 merge 된건가? 저거랑 연관있는건 맞는거 같은데..
    }

    /**
     * 검색
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }
}
