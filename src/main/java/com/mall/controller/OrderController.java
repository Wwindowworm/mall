package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.CreateOrderDTO;
import com.mall.entity.Order;
import com.mall.service.OrderService;
import com.mall.vo.OrderDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** 从购物车下单 */
    @PostMapping("/create")
    public Result<Order> create(@Valid @RequestBody CreateOrderDTO dto,
                                @RequestAttribute("userId") Long userId) {
        Order order = orderService.createFromCart(userId, dto.getAddressId(), dto.getRemark());
        return Result.ok(order);
    }

    /** 直接购买（需传 productId, quantity, addressId） */
    @PostMapping("/createDirect")
    public Result<Order> createDirect(@RequestBody Map<String, Object> params,
                                      @RequestAttribute("userId") Long userId) {
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        Long addressId = Long.valueOf(params.get("addressId").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        Order order = orderService.createDirect(userId, productId, quantity, addressId, remark);
        return Result.ok(order);
    }

    /** 订单列表 */
    @GetMapping("/list")
    public Result<List<Order>> list(@RequestParam(required = false) Integer status,
                                    @RequestAttribute("userId") Long userId) {
        return Result.ok(orderService.list(userId, status));
    }

    /** 订单详情 */
    @GetMapping("/detail")
    public Result<OrderDetailVO> detail(@RequestParam Long orderId,
                                        @RequestAttribute("userId") Long userId) {
        return Result.ok(orderService.getDetail(userId, orderId));
    }

    /** 取消订单 */
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestParam Long orderId,
                               @RequestAttribute("userId") Long userId) {
        orderService.cancel(userId, orderId);
        return Result.ok();
    }

    /** 模拟支付 */
    @PostMapping("/pay")
    public Result<Void> pay(@RequestParam String orderNo,
                            @RequestAttribute("userId") Long userId) {
        orderService.pay(userId, orderNo);
        return Result.ok();
    }

    /** 确认收货 */
    @PostMapping("/confirmReceive")
    public Result<Void> confirmReceive(@RequestParam Long orderId,
                                        @RequestAttribute("userId") Long userId) {
        orderService.confirmReceive(userId, orderId);
        return Result.ok();
    }
}
