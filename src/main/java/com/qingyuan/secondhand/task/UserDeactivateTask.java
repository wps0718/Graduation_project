package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.UserStatus;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.user-deactivate", havingValue = "true", matchIfMissing = true)
public class UserDeactivateTask {

    private final UserMapper userMapper;

    @Scheduled(cron = "0 0 5 * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[用户注销清理任务] 开始执行，时间：{}", start);
        try {
            LocalDateTime deadline = LocalDateTime.now().minusDays(30);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getStatus, UserStatus.DEREGISTERING.getCode())
                    .lt(User::getDeactivateTime, deadline);
            List<User> users = userMapper.selectList(wrapper);
            if (users == null || users.isEmpty()) {
                log.info("[用户注销清理任务] 无需清理的用户");
                return;
            }
            for (User user : users) {
                try {
                    User update = new User();
                    update.setId(user.getId());
                    update.setNickName("已注销用户");
                    update.setAvatarUrl("");
                    update.setPhone(null);
                    update.setOpenId(null);
                    update.setSessionKey(null);
                    update.setStatus(UserStatus.BANNED.getCode());
                    update.setDeactivateTime(null);
                    update.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(update);
                    processedCount++;
                } catch (Exception e) {
                    log.error("[用户注销清理任务] 处理用户失败，用户ID：{}，错误：{}", user.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[用户注销清理任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[用户注销清理任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
