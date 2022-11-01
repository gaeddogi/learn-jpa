package com.jpa.jpashop.service;

import com.jpa.jpashop.domain.Address;
import com.jpa.jpashop.domain.Member;
import com.jpa.jpashop.domain.Order;
import com.jpa.jpashop.domain.OrderStatus;
import com.jpa.jpashop.domain.item.Book;
import com.jpa.jpashop.exception.NotEnoughStockException;
import com.jpa.jpashop.repository.order.OrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;


    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = getMember();
        Book book = getBook("시골 jpa", 10000, 10);

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        Assert.assertEquals("상품 주문 시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        Assert.assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
        Assert.assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, getOrder.getTotalPrice());
        Assert.assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, book.getStockQuantity());
    }

    @Test
    @Rollback(value = false)
    public void 주문취소() throws Exception {
        //given
        Member member = getMember(); // Member 영속화 됨.
        Book book = getBook("시골 jpa", 10000, 10); // Item 영속화 됨.
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount); // Order 영속화,
                                                                                    // cascade에 의해 OrderItem/Delivery 영속화

        System.out.println("====전" + book.getStockQuantity()); //8 영속화된 item(book)에서 가져옴.

        //when
        orderService.cacelOrder(orderId); // Book 재고 수량이 바뀌어서 Book은 변경감지 되었다. 영속성 컨텍스트의 item의 재고값이 바뀜.
                                            // 영속성 컨텍스트의 order의 orderStatus도 바뀜.


        System.out.println("====후" + book.getStockQuantity()); // 영속성 컨텍스트에서 가져온 변경된 재고수량 : 10

        //then
        Order order = orderRepository.findOne(orderId);
        Assert.assertEquals("주문 취소 시 상태는 CANCEL", OrderStatus.CANCEL, order.getStatus());
        Assert.assertEquals("주문 취소 시 재고가 복구되어야 한다.", 10, book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 재고수량초과() throws Exception {
        //given
        Member member = getMember();
        Book book = getBook("시골 jpa", 10000, 10);

        int orderCount = 11;

        //when
        orderService.order(member.getId(), book.getId(), orderCount);
        //then
        Assert.fail("재고 수량 익셉션 발생해야 한다.");
    }


    private Book getBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress((new Address("서울", "강가", "123123")));
        em.persist(member);
        return member;
    }
}