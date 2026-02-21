package com.qingyuan.secondhand.controller.mini;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.FavoriteDTO;
import com.qingyuan.secondhand.service.FavoriteService;
import com.qingyuan.secondhand.vo.FavoriteListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mini/favorite")
@RequiredArgsConstructor
public class MiniFavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestBody @Valid FavoriteDTO dto) {
        favoriteService.addFavorite(dto.getProductId());
        return Result.success();
    }

    @PostMapping("/cancel")
    public Result<Void> cancelFavorite(@RequestBody @Valid FavoriteDTO dto) {
        favoriteService.cancelFavorite(dto.getProductId());
        return Result.success();
    }

    @GetMapping("/list")
    public Result<IPage<FavoriteListVO>> getFavoriteList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(favoriteService.getFavoriteList(page, pageSize));
    }

    @GetMapping("/check/{productId}")
    public Result<Boolean> checkFavorite(@PathVariable Long productId) {
        return Result.success(favoriteService.checkFavorite(productId));
    }
}
