package com.jpa.jpashop.repository.order.query;

import com.jpa.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository { // 화면 맞춤 쿼리를 반환하는 레포지토리, 엔티티를 조회하는 레포지토리와 분리하여 관심사 분리를 할 수 있다.

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                        "select o from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new com.jpa.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new com.jpa.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> orders = findOrders();

        List<Long> orderIds = orders.stream().map(o -> o.getOrderId()).collect(toList());

        List<OrderItemQueryDto> items = em.createQuery(
                        "select new com.jpa.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        // orderId를 키로 orderItem 리스트를 밸류인 '맵'으로 변환.
        Map<Long, List<OrderItemQueryDto>> itemsMap = items.stream()
                .collect(groupingBy(OrderItemQueryDto::getOrderId)); // items 수만큼 for문 (n개)

        orders.stream().forEach(o -> o.setOrderItems(itemsMap.get(o.getOrderId()))); // orders 수만큼 for문 (m개) ==> 결과적으로 n+m번 -> O(n)

//        for (OrderItemQueryDto item : items) { // n * m => O(n^2)
//            for (OrderQueryDto order : orders) {
//                if (order.getOrderId() == item.getOrderId()) {
//                    order.setOrderItems(item);
//                }
//            }
//        }
        return orders;
    }

    public List<OrderQueryDto> findAllbyDto_flat() {
        List<OrderQueryFlatDto> results = em.createQuery("select new com.jpa.jpashop.repository.order.query.OrderQueryFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderQueryFlatDto.class)
                .getResultList();

        Map<OrderQueryDto, List<OrderItemQueryDto>> orderItemQueryDtoMap = results.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()), Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())));
                       //Collectors.groupingBy(key, value)

        return orderItemQueryDtoMap.entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());

//         return results.stream()
//                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()), Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
//                 .entrySet().stream()
//                 .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
//                 .collect(toList());

    }
}
