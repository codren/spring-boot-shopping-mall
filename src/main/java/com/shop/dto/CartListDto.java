package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartListDto {

    private Long cartItemId;
    private String itemName;
    private int price;
    private int count;
    private String imgUrl;

    public CartListDto(Long cartItemId, String itemName, int price, int count, String imgUrl){
        this.cartItemId = cartItemId;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
    }
}
