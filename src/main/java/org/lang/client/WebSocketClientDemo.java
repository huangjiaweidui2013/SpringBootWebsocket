package org.lang.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.lang.pojo.AjaxResult;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

@Slf4j
public class WebSocketClientDemo {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        String url = "ws://localhost:4444/ws/message";
        url = url + "?uid=" + 102 + "&docId=" + 10086 + "&source=1";


        WebSocketClient client = new WebSocketClient(URI.create(url)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                log.info("连接成功");
            }

            @Override
            public void onMessage(String s) {
                log.info("client端成功接收到消息: {}", s);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    AjaxResult ajaxResult = objectMapper.readValue(bytes.array(), AjaxResult.class);
                    log.info("client端成功接收到二进制消息: {}", objectMapper.writeValueAsString(ajaxResult));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {

            }

            @Override
            public void onError(Exception e) {

            }
        };
        AjaxResult<String> ajaxResult = new AjaxResult<>(10, "success", "data");
        client.connectBlocking();
        client.sendPing();

        //发送文本信息
//        client.send("WebSocketClientDemo 发送一条消息给服务端");

        //发送二进制信息
        client.send(objectMapper.writeValueAsBytes(ajaxResult));
        Thread.sleep(10000L);
        client.close();
        log.info("client端断开连接");
    }
}
