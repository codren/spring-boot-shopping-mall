package com.shop.service;


import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;        // 상품을 불러와서 재고를 변경해야함
    private final MemberRepository memberRepository;    // 멤버를 불러와서 연결해야함
    private final OrderRepository orderRepository;      // 주문 객체를 저장해야함
    private final ItemImgRepository itemImgRepository;  // 상품 대표 이미지를 출력해야함

    // 단일 상품 주문
    public Long order(OrderDto orderDto, String email) {

        // OrderItem(List) 객체 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        orderItemList.add(OrderItem.createOrderItem(item, orderDto.getCount()));

        // Order 객체 생성
        Member member = memberRepository.findByEmail(email);
        Order order =  Order.createOrder(member, orderItemList);

        // Order 객체 DB 저장 (Cascade로 인해 OrderItem 객체도 같이 저장)
        orderRepository.save(order);
        return order.getId();
    }

    // 주문 내역 조회
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }
        return new PageImpl<>(orderHistDtos, pageable, totalCount);
    }

    // 주문한 유저가 맞는지 검증
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        if (StringUtils.equals(order.getMember().getEmail(), email)) {
            return true;
        }
        return false;
    }

    // 주문 취소
    public void orderCancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.orderCancel();
    }


    // 장바구니 상품(들) 주문
    public Long orders(List<OrderDto> orderDtoList, String email) {

        // 로그인한 유저 조회
        Member member = memberRepository.findByEmail(email);

        // orderDto 객체를 이용하여 item 객체와 count 값을 얻어낸 뒤, 이를 이용하여 OrderItem 객체(들) 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        //Order Entity 클래스에 존재하는 createOrder 메소드로 Order 생성 및 저장
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);
        return order.getId();
    }

}
