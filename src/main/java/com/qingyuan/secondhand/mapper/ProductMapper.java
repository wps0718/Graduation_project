package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.vo.AdminProductPageVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    ProductDetailVO getProductDetailById(@Param("productId") Long productId);

    Page<ProductListVO> getProductList(Page<ProductListVO> page,
                                       @Param("campusId") Long campusId,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword,
                                       @Param("minPrice") java.math.BigDecimal minPrice,
                                       @Param("maxPrice") java.math.BigDecimal maxPrice,
                                       @Param("sortBy") String sortBy);

    Page<ProductListVO> getMyProductList(Page<ProductListVO> page,
                                         @Param("userId") Long userId,
                                         @Param("status") Integer status,
                                         @Param("keyword") String keyword,
                                         @Param("sortBy") String sortBy,
                                         @Param("order") String order);

    Page<AdminProductPageVO> getAdminProductPage(Page<AdminProductPageVO> page,
                                                 @Param("status") Integer status,
                                                 @Param("categoryId") Long categoryId,
                                                 @Param("keyword") String keyword,
                                                 @Param("minPrice") java.math.BigDecimal minPrice,
                                                 @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                 @Param("beginTime") LocalDateTime beginTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("sortBy") String sortBy);

    List<AdminProductPageVO> exportAdminProductList(@Param("status") Integer status,
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("keyword") String keyword,
                                                   @Param("minPrice") java.math.BigDecimal minPrice,
                                                   @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                   @Param("beginTime") LocalDateTime beginTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("sortBy") String sortBy);

    @Select("select count(1) from favorite where user_id = #{userId} and product_id = #{productId}")
    Integer countFavoriteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("select count(1) from trade_order where status = 1 and product_id = #{productId} and (buyer_id = #{userId} or seller_id = #{userId})")
    Integer countActiveOrderByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("select count(1) from trade_order where status = 1 and product_id = #{productId}")
    Integer countActiveOrderByProduct(@Param("productId") Long productId);

    @Update("UPDATE product SET view_count = view_count + 1, update_time = NOW() WHERE id = #{productId} AND is_deleted = 0")
    int incrementViewCount(@Param("productId") Long productId);

    @Update("UPDATE product SET favorite_count = favorite_count + 1, update_time = NOW() WHERE id = #{productId}")
    int incrementFavoriteCount(@Param("productId") Long productId);

    @Update("UPDATE product SET favorite_count = GREATEST(favorite_count - 1, 0), update_time = NOW() WHERE id = #{productId}")
    int decrementFavoriteCount(@Param("productId") Long productId);
}
