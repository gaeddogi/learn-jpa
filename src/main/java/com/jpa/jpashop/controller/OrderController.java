package com.jpa.jpashop.controller;

import com.jpa.jpashop.domain.Member;
import com.jpa.jpashop.domain.Order;
import com.jpa.jpashop.domain.item.Item;
import com.jpa.jpashop.repository.order.OrderSearch;
import com.jpa.jpashop.service.ItemService;
import com.jpa.jpashop.service.MemberService;
import com.jpa.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findAll();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "/order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {
        orderService.order(memberId, itemId, count);

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(OrderSearch orderSearch, Model model) {
        System.out.println("=======" + orderSearch.getOrderStatus() + " " + orderSearch.getMemberName());
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("orderSearch", orderSearch);
//        System.out.println("=====" + orders.get(0).getOrderItems().size());
        return "order/orderList";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable("id") Long orderId) {
        orderService.cacelOrder(orderId);
        return "redirect:/";
    }
}

