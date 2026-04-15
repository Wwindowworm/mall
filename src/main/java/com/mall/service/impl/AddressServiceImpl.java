package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.Address;
import com.mall.mapper.AddressMapper;
import com.mall.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> listByUserId(Long userId) {
        return addressMapper.selectList(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime)
        );
    }

    @Override
    public Address getById(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    public Address getDefaultByUserId(Long userId) {
        return addressMapper.selectOne(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
        );
    }

    @Override
    @Transactional
    public void add(Address address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        addressMapper.insert(address);
        log.info("新增收货地址，userId={}, addressId={}", address.getUserId(), address.getId());
    }

    @Override
    @Transactional
    public void update(Address address) {
        Address existing = addressMapper.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(address.getUserId())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "地址不存在");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        addressMapper.updateById(address);
        log.info("更新收货地址，addressId={}", address.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        addressMapper.deleteById(id);
        log.info("删除收货地址，addressId={}", id);
    }

    @Override
    @Transactional
    public void setDefault(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(1);
        addressMapper.updateById(address);
        log.info("设置默认收货地址，addressId={}", id);
    }

    private void clearDefault(Long userId) {
        List<Address> defaults = addressMapper.selectList(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
        );
        for (Address a : defaults) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
    }
}
