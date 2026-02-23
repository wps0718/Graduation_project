package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.dto.ProductPublishDTO;
import com.qingyuan.secondhand.dto.ProductIdDTO;
import com.qingyuan.secondhand.dto.ProductUpdateDTO;
import com.qingyuan.secondhand.dto.ProductUpdatePriceDTO;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
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

@RestController
@RequestMapping("/mini/product")
@RequiredArgsConstructor
public class MiniProductController {

    private final ProductService productService;

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
    public Result<Void> offShelf(@RequestBody(required = false) @Valid ProductIdDTO dto,
                                 @RequestParam(required = false) Long productId) {
        productService.offShelf(dto != null ? dto.getProductId() : productId);
        return Result.success();
    }

    @PostMapping("/on-shelf")
    public Result<Void> onShelf(@RequestBody(required = false) @Valid ProductIdDTO dto,
                                @RequestParam(required = false) Long productId) {
        productService.onShelf(dto != null ? dto.getProductId() : productId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteProduct(@RequestBody(required = false) @Valid ProductIdDTO dto,
                                      @RequestParam(required = false) Long productId) {
        productService.deleteProduct(dto != null ? dto.getProductId() : productId);
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
                                                         @RequestParam(required = false) Integer status) {
        return Result.success(productService.getMyProductList(page, pageSize, status));
    }
}
