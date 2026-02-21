package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.BannerDTO;
import com.qingyuan.secondhand.entity.Banner;
import com.qingyuan.secondhand.mapper.BannerMapper;
import com.qingyuan.secondhand.service.BannerService;
import com.qingyuan.secondhand.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private final BannerMapper bannerMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<BannerVO> getMiniBannerList(Long campusId) {
        String cacheKey = RedisConstant.BANNER_LIST + campusId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<BannerVO>>() {
                });
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
                .and(w -> w.isNull(Banner::getCampusId).or().eq(Banner::getCampusId, campusId))
                .and(w -> w.isNull(Banner::getStartTime).or().le(Banner::getStartTime, now))
                .and(w -> w.isNull(Banner::getEndTime).or().ge(Banner::getEndTime, now))
                .orderByAsc(Banner::getSort);
        List<Banner> list = bannerMapper.selectList(wrapper);
        List<BannerVO> result = list == null ? List.of() : list.stream().map(this::toBannerVO).toList();

        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    @Override
    public IPage<BannerVO> getAdminBannerPage(Integer page, Integer pageSize, Integer status, Long campusId) {
        Page<Banner> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Banner::getStatus, status);
        }
        if (campusId != null) {
            wrapper.eq(Banner::getCampusId, campusId);
        }
        wrapper.orderByDesc(Banner::getCreateTime);
        Page<Banner> result = bannerMapper.selectPage(pageObj, wrapper);
        Page<BannerVO> voPage = new Page<>(result.getCurrent(), result.getSize());
        voPage.setTotal(result.getTotal());
        List<BannerVO> records = result.getRecords() == null ? List.of() : result.getRecords().stream().map(this::toBannerVO).toList();
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public void addBanner(BannerDTO dto) {
        Banner banner = new Banner();
        banner.setTitle(dto.getTitle());
        banner.setImage(dto.getImage());
        banner.setLinkType(dto.getLinkType());
        banner.setLinkUrl(dto.getLinkUrl());
        banner.setCampusId(dto.getCampusId());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus());
        banner.setStartTime(dto.getStartTime());
        banner.setEndTime(dto.getEndTime());
        int inserted = bannerMapper.insert(banner);
        if (inserted <= 0) {
            throw new BusinessException("新增Banner失败");
        }
        clearBannerCache();
    }

    @Override
    public void updateBanner(Long id, BannerDTO dto) {
        if (id == null) {
            throw new BusinessException("BannerID不能为空");
        }
        Banner existing = bannerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Banner不存在");
        }
        Banner banner = new Banner();
        banner.setId(id);
        banner.setTitle(dto.getTitle());
        banner.setImage(dto.getImage());
        banner.setLinkType(dto.getLinkType());
        banner.setLinkUrl(dto.getLinkUrl());
        banner.setCampusId(dto.getCampusId());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus());
        banner.setStartTime(dto.getStartTime());
        banner.setEndTime(dto.getEndTime());
        banner.setUpdateTime(LocalDateTime.now());
        int updated = bannerMapper.updateById(banner);
        if (updated <= 0) {
            throw new BusinessException("更新Banner失败");
        }
        clearBannerCache();
    }

    @Override
    public void deleteBanner(Long id) {
        if (id == null) {
            throw new BusinessException("BannerID不能为空");
        }
        Banner existing = bannerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Banner不存在");
        }
        int deleted = bannerMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException("删除Banner失败");
        }
        clearBannerCache();
    }

    private void clearBannerCache() {
        Set<String> keys = stringRedisTemplate.keys(RedisConstant.BANNER_LIST + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private BannerVO toBannerVO(Banner banner) {
        BannerVO vo = new BannerVO();
        vo.setId(banner.getId());
        vo.setTitle(banner.getTitle());
        vo.setImage(banner.getImage());
        vo.setLinkType(banner.getLinkType());
        vo.setLinkUrl(banner.getLinkUrl());
        vo.setCampusId(banner.getCampusId());
        vo.setSort(banner.getSort());
        vo.setStatus(banner.getStatus());
        vo.setStartTime(banner.getStartTime());
        vo.setEndTime(banner.getEndTime());
        vo.setCreateTime(banner.getCreateTime());
        return vo;
    }
}
