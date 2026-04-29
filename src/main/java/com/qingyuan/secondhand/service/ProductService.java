package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.ProductPublishDTO;
import com.qingyuan.secondhand.dto.ProductUpdateDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.vo.AdminProductPageVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService extends IService<Product> {
    void publishProduct(ProductPublishDTO dto);

    void updateProduct(ProductUpdateDTO dto);

    void updatePrice(Long productId, BigDecimal price);

    ProductDetailVO getProductDetail(Long productId);

    IPage<ProductListVO> getProductList(Integer page,
                                        Integer pageSize,
                                        Long campusId,
                                        Long categoryId,
                                        String keyword,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        String sortBy);

    IPage<ProductListVO> getMyProductList(Integer page, Integer pageSize, Integer status,
                                          String keyword, String sortBy, String order);

    void offShelf(Long productId);

    void markSold(Long productId);

    void onShelf(Long productId);

    void deleteProduct(Long productId);

    IPage<AdminProductPageVO> getAdminProductPage(Integer page,
                                                  Integer pageSize,
                                                  Integer status,
                                                  Long categoryId,
                                                  String keyword,
                                                  BigDecimal minPrice,
                                                  BigDecimal maxPrice,
                                                  String beginTime,
                                                  String endTime,
                                                  String sortBy);

    List<AdminProductPageVO> exportAdminProductList(Integer status,
                                                   Long categoryId,
                                                   String keyword,
                                                   BigDecimal minPrice,
                                                   BigDecimal maxPrice,
                                                   String beginTime,
                                                   String endTime,
                                                   String sortBy);

    ProductDetailVO getAdminProductDetail(Long productId);

    void approveProduct(Long productId);

    void rejectProduct(Long productId, String rejectReason);

    void batchApproveProducts(List<Long> productIds);

    void forceOffShelf(Long productId);
}
