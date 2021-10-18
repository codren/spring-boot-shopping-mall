package com.shop.service;

import com.shop.dto.CartItemDto;
import com.shop.dto.CartListDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderService orderService;


    // 장바구니 담기
    public Long addCart(CartItemDto cartItemDto, String email) {

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());

        // 장바구니가 존재하지 않는다면 생성
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        Item item = itemRepository.findById(cartItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        // 해당 상품이 장바구니에 존재하지 않는다면 생성 후 추가
        if (cartItem == null) {
            cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);

        // 해당 상품이 장바구니에 이미 존재한다면 수량을 증가
        } else {
            cartItem.addCount(cartItemDto.getCount());
        }
        return cartItem.getId();
    }


    // 장바구니 조회
    @Transactional(readOnly = true)
    public List<CartListDto> getCartList(String email) {

        List<CartListDto> cartListDtos = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());

        if (cart == null) {
            return cartListDtos;
        }

        cartListDtos = cartItemRepository.findCartListDto(cart.getId());
        return cartListDtos;
    }

    // 현재 로그인한 사용자가 장바구니의 주인인지 확인
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {

        // 현재 로그인된 사용자
        Member curmember = memberRepository.findByEmail(email);

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Member savedmember = cartItem.getCart().getMember();

        if (StringUtils.equals(curmember.getEmail(), savedmember.getEmail())) {
            return true;
        }
        return false;
    }

    // 장바구니 상품 수량 변경
    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItem.updateCount(count);
    }

    // 장바구니 상품 삭제
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    // 장바구니 상품(들) 주문
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {

        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email);

        // 주문한 장바구니 상품을 제거
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }
}
