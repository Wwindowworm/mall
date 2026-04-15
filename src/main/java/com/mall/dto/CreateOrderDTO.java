package com.mall.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderDTO {
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
    private String remark;
    private List<Long> cartItemIds;

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Long> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds; }
}
