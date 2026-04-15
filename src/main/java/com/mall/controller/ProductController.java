package com.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.Result;
import com.mall.dto.ReviewDTO;
import com.mall.entity.Product;
import com.mall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<Page<Product>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return Result.ok(productService.listProducts(categoryId, keyword, page, size));
    }

    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.ok(productService.getProductDetail(id));
    }

    @GetMapping("/recommend")
    public Result<List<Product>> getRecommend(@RequestAttribute(value = "userId", required = false) Long userId) {
        return Result.ok(productService.getRecommendProducts(userId));
    }

    @PostMapping("/review")
    public Result<Void> addReview(
            @RequestAttribute("userId") Long userId,
            @RequestBody ReviewDTO dto
    ) {
        productService.addReview(userId, dto.getProductId(), dto.getRating(), dto.getContent(), dto.getImages());
        return Result.ok();
    }
}
