package org.lang.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lang.common.IMyWebSocket;
import org.lang.pojo.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OperateWebsocketController {
    private final IMyWebSocket myWebSocket;

    @GetMapping("/close/{uid}")
    public AjaxResult<Boolean> closeConnection(@PathVariable("uid") String uid) {
        myWebSocket.closeConnection(uid);
        return AjaxResult.success(true);
    }
}
