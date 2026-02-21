package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.BannerService;
import com.qingyuan.secondhand.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mini/banner")
@RequiredArgsConstructor
public class MiniBannerController {

    private final BannerService bannerService;

    @GetMapping("/list")
    public Result<List<BannerVO>> getBannerList(@RequestParam Long campusId) {
        List<BannerVO> list = bannerService.getMiniBannerList(campusId);
        return Result.success(list);
    }
}
