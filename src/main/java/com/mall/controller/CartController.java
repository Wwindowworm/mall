package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AddCartDTO;
import com.mall.service.CartService;
import com.mall.vo.CartItemVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/list")
    public Result<List<CartItemVO>> list(@RequestAttribute("userId") Long userId) {
        return Result.ok(cartService.list(userId));
    }

    @GetMapping("/count")
    public Result<Integer> count(@RequestAttribute("userId") Long userId) {
        return Result.ok(cartService.count(userId));
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AddCartDTO dto,
                            @RequestAttribute("userId") Long userId) {
        cartService.add(userId, dto.getProductId(), dto.getQuantity());
        return Result.ok();
    }

    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam Long productId,
                               @RequestAttribute("userId") Long userId) {
        cartService.remove(userId, productId);
        return Result.ok();
    }

    @PostMapping("/updateQuantity")
    public Result<Void> updateQuantity(@RequestParam Long productId,
                                       @RequestParam Integer quantity,
                                       @RequestAttribute("userId") Long userId) {
        cartService.updateQuantity(userId, productId, quantity);
        return Result.ok();
    }

    @PostMapping("/clear")
    public Result<Void> clear(@RequestAttribute("userId") Long userId) {
        cartService.clear(userId);
        return Result.ok();
    }
}
