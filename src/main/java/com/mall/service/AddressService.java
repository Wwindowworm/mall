package com.mall.service;

import com.mall.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> listByUserId(Long userId);
    Address getById(Long id);
    Address getDefaultByUserId(Long userId);
    void add(Address address);
    void update(Address address);
    void delete(Long id);
    void setDefault(Long id, Long userId);
}
