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

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/page")
    public Result<IPage<AdminProductPageVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) java.math.BigDecimal minPrice,
                                                  @RequestParam(required = false) java.math.BigDecimal maxPrice,
                                                  @RequestParam(required = false) String beginTime,
                                                  @RequestParam(required = false) String endTime,
                                                  @RequestParam(required = false) String sortBy) {
        return Result.success(productService.getAdminProductPage(page, pageSize, status, categoryId, keyword, minPrice, maxPrice, beginTime, endTime, sortBy));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) java.math.BigDecimal minPrice,
                                         @RequestParam(required = false) java.math.BigDecimal maxPrice,
                                         @RequestParam(required = false) String beginTime,
                                         @RequestParam(required = false) String endTime,
                                         @RequestParam(required = false) String sortBy) {
        List<AdminProductPageVO> rows = productService.exportAdminProductList(status, categoryId, keyword, minPrice, maxPrice, beginTime, endTime, sortBy);

        // CSV（UTF-8 BOM，Excel 兼容）
        StringBuilder sb = new StringBuilder();
        sb.append("\ufeff");
        sb.append("ID,商品标题,售价,原价,分类,浏览量,发布者,状态,发布时间\n");
        for (AdminProductPageVO r : rows) {
            sb.append(safeCsv(r.getId()));
            sb.append(',').append(safeCsv(r.getTitle()));
            sb.append(',').append(safeCsv(r.getPrice()));
            sb.append(',').append(safeCsv(r.getOriginalPrice()));
            sb.append(',').append(safeCsv(r.getCategoryName()));
            sb.append(',').append(safeCsv(r.getViewCount()));
            sb.append(',').append(safeCsv(r.getPublisherNickName()));
            sb.append(',').append(safeCsv(r.getStatus()));
            sb.append(',').append(safeCsv(r.getCreateTime()));
            sb.append('\n');
        }

        String filename = "商品列表_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv";
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String safeCsv(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v);
        // 简单转义：包含逗号/引号/换行时，用引号包裹，并把引号变成双引号
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
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
