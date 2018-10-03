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

import cc.agentx.client.Configuration;
import cc.agentx.wrapper.Wrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Locale;

public final class WebSocketInHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(WebSocketInHandler.class);

    private final Channel dstChannel;
    private final Wrapper wrapper;


    public WebSocketInHandler(Channel dstChannel, Wrapper wrapper) {
        this.dstChannel = dstChannel;
        this.wrapper = wrapper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            log.info("{} received {}", ctx.channel(), request);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
        } else if (frame instanceof BinaryWebSocketFrame) {// 二进制数据接收
            if (dstChannel.isActive()) {
                ByteBuf byteBuf = frame.content();
                try {
                    // if (!byteBuf.hasArray()) {
                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.getBytes(0, bytes);
                    bytes = wrapper.unwrap(bytes);
                    if (bytes != null) {
                        dstChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
                        log.info("\tClient ==========> Target \tSend [{} bytes]", bytes.length);
                    }
                    // }
                } finally {
                    //ReferenceCountUtil.release(frame);
                }
            }

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //System.out.println("in is active" + ctx.channel().id());
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (dstChannel.isActive()) {
            {
                dstChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        //释放锁
        //System.out.println("in is inactive" + ctx.channel().id());
        //Configuration.INSTANCE.getSemaphore().release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("\t          Proxy <- Target \tDisconnect");
        log.info("\tClient <- Proxy           \tDisconnect");
        ctx.close();
        //释放锁
        //Configuration.INSTANCE.getSemaphore().release();
    }
}