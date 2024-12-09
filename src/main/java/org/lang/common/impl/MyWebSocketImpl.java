package org.lang.common.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.lang.common.IMyWebSocket;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class MyWebSocketImpl implements IMyWebSocket {
    /**
     * 在线连接数（线程安全）
     */
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    /**
     * 线程安全的无序集合（存储会话）
     */
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private final ConcurrentHashMap<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    private ConcurrentWebSocketSessionDecorator sessionDecorator(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session, 10000, 65536);
    }


    @Override
    public void handleOpen(WebSocketSession session) {
        sessions.add(session);
        Map<String, Object> attributes = session.getAttributes();
        StringBuilder sb = new StringBuilder();
        attributes.forEach((k, v) -> sb.append(k).append(" == ").append(v.toString()).append(";"));
        int count = connectionCount.incrementAndGet();
        log.info("a new connection opened，current online count：{}, 连接的属性: {}", count, sb);
    }

    @Override
    public void handleClose(WebSocketSession session) {
        sessions.remove(session);
        int count = connectionCount.decrementAndGet();
        log.info("a new connection closed，current online count：{}", count);
    }

    @Override
    public void handleMessage(WebSocketSession session, String message) {
        // 只处理前端传来的文本消息，并且直接丢弃了客户端传来的消息
        log.info("{} received a message：{}",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), message);
        try {
            sendMessage(session, "后端返回一条消息，时间：" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) +
                    ", 消息内容：" + message);

            //给另外一个用户发送消息
            sendMessage("101", "给其他用户发送一条消息，时间：" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    + ", 消息内容：" + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, BinaryMessage binaryMessage) {
        ByteBuffer byteBuffer = binaryMessage.getPayload();
        byte[] bytes = ArrayUtil.toArray(byteBuffer);
        log.info("接收到二进制消息，二进制数据长度： {}", bytes.length);
        try {
            sendMessage(session, binaryMessage);

            //给另外一个用户发送消息
            sendMessage("101", binaryMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void sendMessage(WebSocketSession session, String message) throws IOException {
        this.sendMessage(session, new TextMessage(message));
    }

    @Override
    public void sendMessage(String userId, TextMessage message) throws IOException {
        Optional<WebSocketSession> userSession = sessions.stream().filter(session -> {
            if (!session.isOpen()) {
                return false;
            }
            Map<String, Object> attributes = session.getAttributes();
            if (!attributes.containsKey("uid")) {
                return false;
            }
            String uid = (String) attributes.get("uid");
            return uid.equals(userId);
        }).findFirst();
        if (userSession.isPresent()) {
            sessionDecorator(userSession.get()).sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String userId, BinaryMessage binaryMessage) throws IOException {
        Optional<WebSocketSession> userSession = sessions.stream().filter(session -> {
            if (!session.isOpen()) {
                return false;
            }
            Map<String, Object> attributes = session.getAttributes();
            if (!attributes.containsKey("uid")) {
                return false;
            }
            String uid = (String) attributes.get("uid");
            return uid.equals(userId);
        }).findFirst();
        if (userSession.isPresent()) {
            sessionDecorator(userSession.get()).sendMessage(binaryMessage);
        }
    }

    @Override
    public void sendMessage(String userId, String message) throws IOException {
        this.sendMessage(userId, new TextMessage(message));
    }

    @Override
    public void sendMessage(WebSocketSession session, TextMessage message) throws IOException {
//        session.sendMessage(message);
        sessionDecorator(session).sendMessage(message);
    }

    @Override
    public void sendMessage(WebSocketSession session, BinaryMessage binaryMessage) throws IOException {
//        session.sendMessage(binaryMessage);
        sessionDecorator(session).sendMessage(binaryMessage);
    }

    @Override
    public void broadCast(String message) throws IOException {
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            this.sendMessage(session, message);
        }
    }

    @Override
    public void broadCast(TextMessage message) throws IOException {
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
//            session.sendMessage(message);
            sessionDecorator(session).sendMessage(message);
        }
    }

    @Override
    public void handleError(WebSocketSession session, Throwable error) {
        log.error("websocket error：{}，session id：{}", error.getMessage(), session.getId());
        log.error("", error);
    }

    @Override
    public Set<WebSocketSession> getSessions() {
        return sessions;
    }

    @Override
    public int getConnectionCount() {
        return connectionCount.get();
    }

    @Override
    public void closeConnection(String userId) {
        Optional<WebSocketSession> userSession = sessions.stream().filter(session -> {
            if (!session.isOpen()) {
                return false;
            }
            Map<String, Object> attributes = session.getAttributes();
            if (!attributes.containsKey("uid")) {
                return false;
            }
            String uid = (String) attributes.get("uid");
            return uid.equals(userId);
        }).findFirst();
        if (userSession.isPresent()) {
            try {
                userSession.get().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Scheduled(cron = "0/10 * * * * *")
    public void ping() {
        if (CollUtil.isNotEmpty(sessions)) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    Map<String, Object> attributes = session.getAttributes();
                    String uid = (String) attributes.get("uid");
                    try {
//                        sendMessage(uid, "PING");
                        session.sendMessage(new PingMessage(ByteBuffer.wrap(new byte[0])));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }
}
