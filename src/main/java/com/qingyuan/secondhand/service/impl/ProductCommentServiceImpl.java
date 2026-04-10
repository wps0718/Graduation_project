package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.ProductCommentAddDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.ProductComment;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ProductCommentMapper;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.ProductCommentService;
import com.qingyuan.secondhand.vo.ProductCommentVO;
import com.qingyuan.secondhand.vo.ReceivedReplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void addComment(ProductCommentAddDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        ProductComment comment = new ProductComment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(currentUserId);
        comment.setIsRead(0);

        if (dto.getParentId() != null) {
            ProductComment parent = baseMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父留言不存在");
            }
            comment.setRootId(parent.getRootId() == null ? parent.getId() : parent.getRootId());
            comment.setReplyToUserId(parent.getUserId());
        } else {
            // 如果是第一层留言，且当前用户不是商品发布者，则认为是在向发布者留言
            if (!currentUserId.equals(product.getUserId())) {
                comment.setReplyToUserId(product.getUserId());
            }
        }

        baseMapper.insert(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        Long currentUserId = UserContext.getCurrentUserId();
        ProductComment comment = baseMapper.selectById(commentId);
        if (comment == null) {
            return;
        }
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权删除他人留言");
        }
        baseMapper.deleteById(commentId);
    }

    @Override
    public List<ProductCommentVO> getCommentList(Long productId) {
        // 获取所有留言
        List<ProductComment> allComments = baseMapper.selectList(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getProductId, productId)
                .orderByAsc(ProductComment::getCreateTime));

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有涉及的用户ID
        List<Long> userIds = new ArrayList<>();
        allComments.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) userIds.add(c.getReplyToUserId());
        });
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        // 转换为VO并分组
        List<ProductCommentVO> allVOs = allComments.stream().map(c -> {
            ProductCommentVO vo = new ProductCommentVO();
            BeanUtils.copyProperties(c, vo);
            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setNickName(user.getNickName());
                vo.setAvatarUrl(user.getAvatarUrl());
            }
            if (c.getReplyToUserId() != null) {
                User replyUser = userMap.get(c.getReplyToUserId());
                if (replyUser != null) {
                    vo.setReplyToNickName(replyUser.getNickName());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 组装树形结构（两层：根留言 + 回复列表）
        List<ProductCommentVO> rootComments = allVOs.stream()
                .filter(vo -> vo.getRootId() == null)
                .collect(Collectors.toList());

        Map<Long, List<ProductCommentVO>> replyMap = allVOs.stream()
                .filter(vo -> vo.getRootId() != null)
                .collect(Collectors.groupingBy(ProductCommentVO::getRootId));

        rootComments.forEach(root -> {
            root.setReplies(replyMap.getOrDefault(root.getId(), new ArrayList<>()));
        });

        return rootComments;
    }

    @Override
    public IPage<ReceivedReplyVO> getReceivedReplies(Integer page, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();
        Page<ProductComment> commentPage = baseMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getReplyToUserId, currentUserId)
                        .orderByDesc(ProductComment::getCreateTime));

        if (commentPage.getRecords().isEmpty()) {
            return new Page<>(page, pageSize, 0);
        }

        List<Long> productIds = commentPage.getRecords().stream().map(ProductComment::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<Long> userIds = commentPage.getRecords().stream().map(ProductComment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<ReceivedReplyVO> records = commentPage.getRecords().stream().map(c -> {
            ReceivedReplyVO vo = new ReceivedReplyVO();
            vo.setId(c.getId());
            vo.setProductId(c.getProductId());
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            vo.setIsRead(c.getIsRead());

            Product p = productMap.get(c.getProductId());
            if (p != null) {
                vo.setProductTitle(p.getTitle());
                // 解析图片JSON数组取第一张
                String images = p.getImages();
                if (images != null && images.startsWith("[")) {
                    // 简易处理：取第一个引号内的内容
                    int firstQuote = images.indexOf("\"");
                    int secondQuote = images.indexOf("\"", firstQuote + 1);
                    if (firstQuote != -1 && secondQuote != -1) {
                        vo.setProductImage(images.substring(firstQuote + 1, secondQuote));
                    }
                }
            }

            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setFromUserId(u.getId());
                vo.setFromNickName(u.getNickName());
                vo.setFromAvatarUrl(u.getAvatarUrl());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<ReceivedReplyVO> resultPage = new Page<>(page, pageSize, commentPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Long getUnreadReplyCount() {
        Long currentUserId = UserContext.getCurrentUserId();
        return baseMapper.selectCount(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getReplyToUserId, currentUserId)
                .eq(ProductComment::getIsRead, 0));
    }

    @Override
    @Transactional
    public void markRepliesRead() {
        Long currentUserId = UserContext.getCurrentUserId();
        ProductComment update = new ProductComment();
        update.setIsRead(1);
        baseMapper.update(update, new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getReplyToUserId, currentUserId)
                .eq(ProductComment::getIsRead, 0));
    }
}
