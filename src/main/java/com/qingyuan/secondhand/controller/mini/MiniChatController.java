package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.ChatSessionCreateDTO;
import com.qingyuan.secondhand.dto.ChatMessageSendDTO;
import com.qingyuan.secondhand.service.ChatMessageService;
import com.qingyuan.secondhand.service.ChatSessionService;
import com.qingyuan.secondhand.vo.ChatSessionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mini/chat")
@Slf4j
@RequiredArgsConstructor
public class MiniChatController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/session/create")
    public Result<ChatSessionVO> createSession(@RequestBody @Valid ChatSessionCreateDTO dto) {
        ChatSessionVO vo = chatSessionService.createSession(dto);
        return Result.success(vo);
    }

    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> getSessionList() {
        List<ChatSessionVO> list = chatSessionService.getSessionList();
        return Result.success(list);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getSessionListForMini() {
        List<ChatSessionVO> list = chatSessionService.getSessionList();
        Map<String, Object> data = new HashMap<>();
        data.put("total", list == null ? 0 : list.size());
        data.put("records", list);
        return Result.success(data);
    }

    @PostMapping("/session/delete")
    public Result<Void> deleteSession(@RequestBody Map<String, Long> params) {
        Long sessionId = params == null ? null : params.get("sessionId");
        if (sessionId == null) {
            return Result.error("sessionId不能为空");
        }
        chatSessionService.deleteSession(sessionId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteSessionForMini(@RequestBody Map<String, Long> params) {
        return deleteSession(params);
    }

    @PostMapping("/session/top")
    public Result<Void> toggleTop(@RequestBody Map<String, Long> params) {
        Long sessionId = params == null ? null : params.get("sessionId");
        if (sessionId == null) {
            return Result.error("sessionId不能为空");
        }
        chatSessionService.toggleTop(sessionId);
        return Result.success();
    }

    @GetMapping("/unread-total")
    public Result<Integer> getUnreadTotal() {
        Integer total = chatSessionService.getUnreadTotal();
        return Result.success(total);
    }

    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(@RequestParam String sessionKey,
                                                   @RequestParam Integer page,
                                                   @RequestParam Integer pageSize) {
        Map<String, Object> data = chatMessageService.getMessageHistory(sessionKey, page, pageSize);
        return Result.success(data);
    }

    @PostMapping("/read")
    public Result<Void> markRead(@RequestBody Map<String, String> params) {
        String sessionKey = params == null ? null : params.get("sessionKey");
        if (sessionKey == null) {
            return Result.error("sessionKey不能为空");
        }
        chatMessageService.markSessionRead(sessionKey);
        return Result.success();
    }

    @PostMapping("/message/send")
    public Result<Long> sendMessage(@RequestBody @Valid ChatMessageSendDTO dto) {
        Long msgId = chatMessageService.sendMessage(dto);
        return Result.success(msgId);
    }
}
