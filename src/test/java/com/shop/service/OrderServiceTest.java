package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.constant.OrderStatus;
import com.shop.dto.OrderDto;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    public Item saveItem(){
        Item item = new Item();
        item.setItemName("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStock(100);
        return itemRepository.save(item);
    }

    public Member saveMember(){
        Member member = new Member();
        member.setEmail("test@test.com");
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("주문 테스트")
    public void order() {
        Item item = saveItem();
        Member member = saveMember();

        // 상품 상세 페이지 화면에서 넘어오는 값들 설정
        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);
        orderDto.setItemId(item.getId());

        // 주문 객체 DB에 저장
        Long orderId = orderService.order(orderDto, member.getEmail());

        // 저장된 주문 객체 조회
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        // 1. DB에 저장된 주문 객체에서 주문 상품 추출 (1개)
        List<OrderItem> orderItems = order.getOrderItems();

        // 2. 위에서 만든 주문 상품 총 가격 (1개)
        int totalPrice = orderDto.getCount() * item.getPrice();

        // 1의 가격과 2가 같은지 테스트
        assertEquals(totalPrice, order.getTotalPrice());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder() {

        Item item = saveItem();
        Member member = saveMember();

        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);
        orderDto.setItemId(item.getId());

        // 주문 객체 저장
        Long orderId = orderService.order(orderDto, member.getEmail());

        // 주문된 객체를 조회한 뒤에 주문 취소
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        orderService.orderCancel(orderId);

        // 주문의 상태가 "CANCEL" 이고 처음 수량 100이 맞다면 테스트 통과
        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        assertEquals(100, item.getStock());

    }

}