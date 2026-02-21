package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.ReviewSubmitDTO;
import com.qingyuan.secondhand.service.ReviewService;
import com.qingyuan.secondhand.vo.ReviewDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mini/review")
@RequiredArgsConstructor
public class MiniReviewController {

    private final ReviewService reviewService;

    @PostMapping("/submit")
    public Result<Void> submitReview(@RequestBody @Valid ReviewSubmitDTO dto) {
        reviewService.submitReview(dto);
        return Result.success();
    }

    @GetMapping("/detail/{orderId}")
    public Result<ReviewDetailVO> getReviewDetail(@PathVariable Long orderId) {
        ReviewDetailVO detail = reviewService.getReviewDetail(orderId);
        return Result.success(detail);
    }
}
