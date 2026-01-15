package com.epoll.server_v1;

import com.epoll.server_v1.security.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTService jwtService;

    public JwtHandshakeInterceptor(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (request instanceof ServletServerHttpRequest servletRequest) {

            HttpServletRequest req = servletRequest.getServletRequest();
            String protocolHeader = req.getHeader("Sec-WebSocket-Protocol");

            if (protocolHeader != null) {
                // Expected: "jwt, <token>"
                String[] parts = protocolHeader.split(",");

                if (parts.length >= 2 && parts[0].trim().equals("jwt")) {
                    String token = parts[1].trim();

                    try {
                        String email = jwtService.extractUsername(token);

                        // ✅ Attach authenticated identity
                        attributes.put("email", email);

                        // ✅ MUST echo chosen protocol (browser requirement)
                        response.getHeaders()
                                .add("Sec-WebSocket-Protocol", "jwt");

                        return true;
                    } catch (Exception ignored) {}
                }
            }
        }

        return false; // ❌ reject handshake
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }
}
