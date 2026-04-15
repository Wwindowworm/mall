package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.SignRecord;
import com.mall.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sign")
public class SignController {

    @Autowired
    private SignService signService;

    @PostMapping("/in")
    public Result<SignRecord> sign(@RequestAttribute("userId") Long userId) {
        return Result.ok(signService.sign(userId));
    }

    @GetMapping("/calendar")
    public Result<List<SignRecord>> getCalendar(
            @RequestAttribute("userId") Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return Result.ok(signService.getSignCalendar(userId, year, month));
    }
}
