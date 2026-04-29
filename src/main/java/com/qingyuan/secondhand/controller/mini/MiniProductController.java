package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.dto.ProductPublishDTO;
import com.qingyuan.secondhand.dto.ProductIdDTO;
import com.qingyuan.secondhand.dto.ProductUpdateDTO;
import com.qingyuan.secondhand.dto.ProductUpdatePriceDTO;
import com.qingyuan.secondhand.dto.ProductCommentAddDTO;
import com.qingyuan.secondhand.service.ProductCommentService;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.vo.ProductCommentVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
import com.qingyuan.secondhand.vo.ReceivedReplyVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/mini/product")
@RequiredArgsConstructor
public class MiniProductController {

    private final ProductService productService;
    private final ProductCommentService productCommentService;

    @PostMapping("/publish")
    public Result<Void> publishProduct(@RequestBody @Valid ProductPublishDTO dto) {
        productService.publishProduct(dto);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateProduct(@RequestBody @Valid ProductUpdateDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    @PostMapping("/update-price")
    public Result<Void> updatePrice(@RequestBody @Valid ProductUpdatePriceDTO dto) {
        productService.updatePrice(dto.getProductId(), dto.getPrice());
        return Result.success();
    }

    @PostMapping("/off-shelf")
    public Result<Void> offShelf(@RequestBody @Valid ProductIdDTO dto) {
        productService.offShelf(dto.getProductId());
        return Result.success();
    }

    @PostMapping("/mark-sold")
    public Result<Void> markSold(@RequestBody @Valid ProductIdDTO dto) {
        productService.markSold(dto.getProductId());
        return Result.success();
    }

    @PostMapping("/on-shelf")
    public Result<Void> onShelf(@RequestBody @Valid ProductIdDTO dto) {
        productService.onShelf(dto.getProductId());
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteProduct(@RequestBody @Valid ProductIdDTO dto) {
        productService.deleteProduct(dto.getProductId());
        return Result.success();
    }

    @GetMapping("/detail/{productId}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(productService.getProductDetail(productId));
    }

    @GetMapping("/list")
    public Result<IPage<ProductListVO>> getProductList(@RequestParam Integer page,
                                                       @RequestParam Integer pageSize,
                                                       @RequestParam(required = false) Long campusId,
                                                       @RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) BigDecimal minPrice,
                                                       @RequestParam(required = false) BigDecimal maxPrice,
                                                       @RequestParam(defaultValue = "latest") String sortBy) {
        return Result.success(productService.getProductList(page, pageSize, campusId, categoryId, keyword, minPrice, maxPrice, sortBy));
    }

    @GetMapping("/my-list")
    public Result<IPage<ProductListVO>> getMyProductList(@RequestParam Integer page,
                                                         @RequestParam Integer pageSize,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String sortBy,
                                                         @RequestParam(required = false) String order) {
        return Result.success(productService.getMyProductList(page, pageSize, status, keyword, sortBy, order));
    }

    @PostMapping("/comment/add")
    public Result<Void> addComment(@RequestBody @Valid ProductCommentAddDTO dto) {
        productCommentService.addComment(dto);
        return Result.success();
    }

    @PostMapping("/comment/delete/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        productCommentService.deleteComment(commentId);
        return Result.success();
    }

    @GetMapping("/comment/list/{productId}")
    public Result<List<ProductCommentVO>> getCommentList(@PathVariable Long productId) {
        return Result.success(productCommentService.getCommentList(productId));
    }

    @GetMapping("/comment/received-replies")
    public Result<IPage<ReceivedReplyVO>> getReceivedReplies(@RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productCommentService.getReceivedReplies(page, pageSize));
    }

    @GetMapping("/comment/unread-reply-count")
    public Result<Long> getUnreadReplyCount() {
        return Result.success(productCommentService.getUnreadReplyCount());
    }

    @PostMapping("/comment/mark-read")
    public Result<Void> markRead() {
        productCommentService.markRepliesRead();
        return Result.success();
    }
}
