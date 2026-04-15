package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.entity.Category;
import com.mall.mapper.CategoryMapper;
import com.mall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
        );
    }

    @Override
    public List<Category> listByParentId(Long parentId) {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId != null ? parentId : 0)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
        );
    }
}
