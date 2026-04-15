package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AddressDTO;
import com.mall.entity.Address;
import com.mall.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public Result<List<Address>> list(@RequestAttribute("userId") Long userId) {
        return Result.ok(addressService.listByUserId(userId));
    }

    @GetMapping("/default")
    public Result<Address> getDefault(@RequestAttribute("userId") Long userId) {
        Address addr = addressService.getDefaultByUserId(userId);
        return Result.ok(addr);
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AddressDTO dto,
                             @RequestAttribute("userId") Long userId) {
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault());
        addressService.add(address);
        return Result.ok();
    }

    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody AddressDTO dto,
                               @RequestAttribute("userId") Long userId) {
        Address address = new Address();
        address.setId(dto.getId());
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault());
        addressService.update(address);
        return Result.ok();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id,
                               @RequestAttribute("userId") Long userId) {
        addressService.delete(id);
        return Result.ok();
    }

    @PostMapping("/setDefault")
    public Result<Void> setDefault(@RequestParam Long id,
                                   @RequestAttribute("userId") Long userId) {
        addressService.setDefault(id, userId);
        return Result.ok();
    }
}
