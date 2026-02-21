package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.ReviewSubmitDTO;
import com.qingyuan.secondhand.entity.Review;
import com.qingyuan.secondhand.vo.ReviewDetailVO;

public interface ReviewService extends IService<Review> {
    void submitReview(ReviewSubmitDTO dto);

    ReviewDetailVO getReviewDetail(Long orderId);
}
