package com.mall.service;

import com.mall.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    List<Category> listByParentId(Long parentId);
}
