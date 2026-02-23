package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.ProductRejectDTO;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.vo.AdminProductPageVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/page")
    public Result<IPage<AdminProductPageVO>> page(@RequestParam Integer page,
                                                  @RequestParam Integer pageSize,
                                                  @RequestParam(required = false) Integer status) {
        return Result.success(productService.getAdminProductPage(page, pageSize, status));
    }

    @GetMapping("/detail/{productId}")
    public Result<ProductDetailVO> detail(@PathVariable Long productId) {
        return Result.success(productService.getAdminProductDetail(productId));
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestParam Long productId) {
        productService.approveProduct(productId);
        return Result.success();
    }

    @PostMapping("/reject")
    public Result<Void> reject(@RequestBody @Valid ProductRejectDTO dto) {
        productService.rejectProduct(dto.getProductId(), dto.getRejectReason());
        return Result.success();
    }

    @PostMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody List<Long> productIds) {
        productService.batchApproveProducts(productIds);
        return Result.success();
    }

    @PostMapping("/force-off")
    public Result<Void> forceOff(@RequestParam Long productId) {
        productService.forceOffShelf(productId);
        return Result.success();
    }
}
