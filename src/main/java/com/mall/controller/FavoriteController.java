package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.Product;
import com.mall.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/list")
    public Result<List<Product>> list(@RequestAttribute("userId") Long userId) {
        return Result.ok(favoriteService.list(userId));
    }

    @GetMapping("/count")
    public Result<Integer> count(@RequestAttribute("userId") Long userId) {
        return Result.ok(favoriteService.count(userId));
    }

    @GetMapping("/check")
    public Result<Map<String, Boolean>> check(@RequestParam Long productId,
                                              @RequestAttribute("userId") Long userId) {
        return Result.ok(Map.of("favorited", favoriteService.isFavorited(userId, productId)));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long productId,
                            @RequestAttribute("userId") Long userId) {
        favoriteService.add(userId, productId);
        return Result.ok();
    }

    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam Long productId,
                               @RequestAttribute("userId") Long userId) {
        favoriteService.remove(userId, productId);
        return Result.ok();
    }

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(@RequestParam Long productId,
                                              @RequestAttribute("userId") Long userId) {
        boolean isFav = favoriteService.isFavorited(userId, productId);
        if (isFav) {
            favoriteService.remove(userId, productId);
        } else {
            favoriteService.add(userId, productId);
        }
        return Result.ok(Map.of("favorited", !isFav));
    }
}
