package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.ProductCommentAddDTO;
import com.qingyuan.secondhand.entity.ProductComment;
import com.qingyuan.secondhand.vo.ProductCommentVO;
import com.qingyuan.secondhand.vo.ReceivedReplyVO;

import java.util.List;

public interface ProductCommentService extends IService<ProductComment> {
    void addComment(ProductCommentAddDTO dto);
    void deleteComment(Long commentId);
    List<ProductCommentVO> getCommentList(Long productId);
    IPage<ReceivedReplyVO> getReceivedReplies(Integer page, Integer pageSize);
    Long getUnreadReplyCount();
    void markRepliesRead();
}
