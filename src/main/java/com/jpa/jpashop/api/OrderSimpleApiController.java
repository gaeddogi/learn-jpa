package com.jpa.jpashop.api;

import com.jpa.jpashop.domain.Address;
import com.jpa.jpashop.domain.Order;
import com.jpa.jpashop.domain.OrderStatus;
import com.jpa.jpashop.repository.order.OrderRepository;
import com.jpa.jpashop.repository.order.OrderSearch;
import com.jpa.jpashop.repository.order.query.OrderQueryRepository;
import com.jpa.jpashop.repository.order.query.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne의 성능 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
//        return orders.stream()
//                .map(o -> new SimpleOrderDto(o.getId(), o.getMember().getName(), o.getOrderDate(), o.getStatus(), o.getDelivery().getAddress()))
//                .collect(Collectors.toList());
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders") // dto 변환과정 필요없게끔 jpql 결과를 dto로 반환해버리기
    public List<OrderSimpleQueryDto> orderV4() {
        return orderQueryRepository.findOrderDtos();
    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
        }
    }

}
