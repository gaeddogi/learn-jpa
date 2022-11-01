package com.jpa.jpashop.repository.order;

import com.jpa.jpashop.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderSearch {

    private String memberName;
    private OrderStatus orderStatus; // ORDER, CANCEL
}
