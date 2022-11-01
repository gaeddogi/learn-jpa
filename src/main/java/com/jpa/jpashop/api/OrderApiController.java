package com.jpa.jpashop.api;

import com.jpa.jpashop.domain.Address;
import com.jpa.jpashop.domain.Order;
import com.jpa.jpashop.domain.OrderItem;
import com.jpa.jpashop.domain.OrderStatus;
import com.jpa.jpashop.repository.order.OrderRepository;
import com.jpa.jpashop.repository.order.OrderSearch;
import com.jpa.jpashop.repository.order.query.OrderQueryDto;
import com.jpa.jpashop.repository.order.query.OrderQueryFlatDto;
import com.jpa.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // 프록시 강제 초기화 하려고 하는거임
        for (Order order : all) {
//            order.getMember().getName();
//            order.getDelivery().getAddress();
//            List<OrderItem> orderItems = order.getOrderItems();
////            for (OrderItem orderItem : orderItems) {
////                orderItem.getItem().getName();
////            }
//            orderItems.stream().forEach(oi -> oi.getItem().getName());
        }

        return all;
    }

    // ENTITY -> DTO 반환으로 수정해본다.
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    // 일대다 관계를 페치조인으로 가져온다. (단점. 페이징 불가능, 여러 컬렉션이 있을경우 페치조인조차 불가능)
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> all = orderRepository.findAllWithItem();
        return all.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    // 페이징 한계돌파
    // 1. toOne 관계는 페치조인으로 가져온다.
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_paging(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
    }

    // Jpa에서 DTO 직접 조회 -> N(order갯수) + 1
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    // Jpa에서 DTO 직접 조회 -> 1(order) + 1(orderItems) V4랑 메모리 사용량은 같지만 한번에 orderItems를 쫙 떙겨오기 떄문에 순간부하는 있음.
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    // Jpa에서 DTO 직접 조회 -> 쿼리 한개로, 대신 페이징 불가.
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6() {
        return orderQueryRepository.findAllbyDto_flat();
    }


    @Data

    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // LAZY 로딩
//            this.orderItems = order.getOrderItems();

            this.orderItems = order.getOrderItems().stream().map(o -> new OrderItemDto(o)).collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문가격
        private int count; // 주문수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }


}
