package com.jpa.jpashop.repository.order.query;

import com.jpa.jpashop.domain.Address;
import com.jpa.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Long id, String name, LocalDateTime orderDate, OrderStatus status, Address address) {
        this.orderId = id;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = status;
        this.address = address;
    }
}
