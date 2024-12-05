package org.lang.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            //WebSocket 无法使用 header 传递参数，因此这里使用 url params 携带参数。
            //模拟用户（通常利用JWT令牌解析用户信息）
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
            String userId = servletRequest.getParameter("uid");
            String docId = servletRequest.getParameter("docId");
            String source = servletRequest.getParameter("source");
            //@TODO 判断用户是否存在
            attributes.put("uid", userId);
            attributes.put("docId", docId);
            attributes.put("source", source);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
