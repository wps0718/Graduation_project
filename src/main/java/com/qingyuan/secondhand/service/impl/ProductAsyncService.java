package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.SearchKeyword;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.SearchKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductAsyncService {

    private final ProductMapper productMapper;
    private final SearchKeywordMapper searchKeywordMapper;

    @Async
    public void asyncUpdateViewCount(Long productId) {
        if (productId == null) {
            return;
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return;
        }
        Integer viewCount = product.getViewCount() == null ? 0 : product.getViewCount();
        Product update = new Product();
        update.setId(productId);
        update.setViewCount(viewCount + 1);
        productMapper.updateById(update);
    }

    @Async
    public void asyncRecordSearchKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        SearchKeyword existing = searchKeywordMapper.selectOne(new LambdaQueryWrapper<SearchKeyword>()
                .eq(SearchKeyword::getKeyword, keyword)
                .last("limit 1"));
        if (existing == null) {
            SearchKeyword created = new SearchKeyword();
            created.setKeyword(keyword);
            created.setSearchCount(1);
            created.setIsHot(0);
            created.setSort(0);
            created.setStatus(1);
            searchKeywordMapper.insert(created);
            return;
        }
        SearchKeyword update = new SearchKeyword();
        update.setId(existing.getId());
        update.setSearchCount(existing.getSearchCount() == null ? 1 : existing.getSearchCount() + 1);
        searchKeywordMapper.updateById(update);
    }
}
