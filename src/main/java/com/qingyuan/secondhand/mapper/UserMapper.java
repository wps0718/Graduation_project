package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.vo.AdminUserDetailVO;
import com.qingyuan.secondhand.vo.AdminUserPageVO;
import com.qingyuan.secondhand.vo.SellerProductVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.util.StringUtils;

import java.util.Map;

public interface UserMapper extends BaseMapper<User> {

    default User selectByOpenId(String openId) {
        if (!StringUtils.hasText(openId)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenId, openId)
                .last("limit 1"));
    }

    default User selectByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .last("limit 1"));
    }

    @Select("select name from campus where id = #{campusId} limit 1")
    String selectCampusNameById(Long campusId);

    @Select("select count(1) from product where user_id = #{userId} and status = 1 and is_deleted = 0")
    Integer countOnSaleProducts(Long userId);

    @Select("select count(1) from trade_order where seller_id = #{userId} and status in (3,4)")
    Integer countSoldOrders(Long userId);

    @Select("select count(1) from favorite where user_id = #{userId}")
    Integer countFavoriteProducts(Long userId);

    @Select("select status from campus_auth where user_id = #{userId} order by id desc limit 1")
    Integer selectLatestCampusAuthAuditStatus(Long userId);

    @Select("SELECT COUNT(1) FROM trade_order WHERE (buyer_id = #{userId} OR seller_id = #{userId}) AND status = 1")
    Integer countActiveOrders(Long userId);

    @Update("UPDATE product SET status = 2 WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0")
    int offShelfAllProducts(Long userId);

    @Select("""
            select id, title, price, images, create_time
            from product
            where user_id = #{userId} and status = 1 and is_deleted = 0
            order by create_time desc
            """)
    Page<Map<String, Object>> pageOnSaleProducts(Page<Map<String, Object>> page, @Param("userId") Long userId);

    @Select("""
            select
                p.id,
                p.title,
                p.price,
                p.original_price as originalPrice,
                p.condition_level as conditionLevel,
                p.images as coverImage,
                ca.name as campusName,
                p.create_time as createTime
            from product p
            left join campus ca on p.campus_id = ca.id
            where p.user_id = #{userId} and p.status = 1 and p.is_deleted = 0
            order by p.create_time desc
            """)
    Page<SellerProductVO> pageOnSaleSellerProducts(Page<SellerProductVO> page, @Param("userId") Long userId);

    Page<AdminUserPageVO> getAdminUserPage(Page<AdminUserPageVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("authStatus") Integer authStatus,
                                           @Param("campusId") Long campusId);

    AdminUserDetailVO getAdminUserDetail(@Param("id") Long id);
}
