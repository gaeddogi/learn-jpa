package com.jpa.jpashop.repository.order.query;

import lombok.Data;

@Data
public class OrderItemQueryDto {

    private Long orderId;
    private String itemName;
    private int orderPrice;
    private int price;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int price) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.price = price;
    }


}
