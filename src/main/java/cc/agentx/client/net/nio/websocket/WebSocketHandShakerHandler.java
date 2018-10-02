/*
 * Copyright 2017 ZhangJiupeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.agentx.client.net.nio.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class WebSocketHandShakerHandler extends SimpleChannelInboundHandler<Object> {
    private static final InternalLogger log;
    private final WebSocketClientHandshaker handshaker;

    static {
        log = InternalLoggerFactory.getInstance(WebSocketHandShakerHandler.class);
    }

    private final Promise<Channel> promise;
    private final long initializeTimeMillis;

    public WebSocketHandShakerHandler(WebSocketClientHandshaker handshaker, Promise<Channel> promise, long initializeTimeMillis) {
        log.info("\t          Proxy -> Target \tPing");
        this.handshaker = handshaker;
        this.promise = promise;
        this.initializeTimeMillis = initializeTimeMillis;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                log.info("WebSocket Client connected!");
                ctx.pipeline().remove(this);
                promise.setSuccess(ctx.channel());
            } catch (WebSocketHandshakeException e) {
                log.info("WebSocket Client failed to connect");
                promise.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!promise.isDone()) {
            promise.setFailure(cause);
        }
        ctx.close();
    }

}
