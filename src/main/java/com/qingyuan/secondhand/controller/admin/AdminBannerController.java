package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.BannerDTO;
import com.qingyuan.secondhand.service.BannerService;
import com.qingyuan.secondhand.vo.BannerVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping("/page")
    public Result<IPage<BannerVO>> getBannerPage(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) Long campusId) {
        IPage<BannerVO> result = bannerService.getAdminBannerPage(page, pageSize, status, campusId);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addBanner(@Valid @RequestBody BannerDTO dto) {
        bannerService.addBanner(dto);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateBanner(@RequestParam Long id, @Valid @RequestBody BannerDTO dto) {
        bannerService.updateBanner(id, dto);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteBanner(@RequestParam Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }
}
