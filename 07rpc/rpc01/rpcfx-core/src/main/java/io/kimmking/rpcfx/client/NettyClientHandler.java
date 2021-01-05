package io.kimmking.rpcfx.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyClientHandler extends ChannelInboundHandlerAdapter{
    private String reqJson;

    public NettyClientHandler(String reqJson) {
        this.reqJson = reqJson;
    }

    // 客户端连接服务器后被调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接服务器，开始发送数据……");
        byte[] req = reqJson.getBytes("UTF-8");//消息
        ByteBuf firstMessage = Unpooled.wrappedBuffer(req);//发送类

        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,"http://127.0.0.1:8090",firstMessage);
        //https://blog.csdn.net/xbt312/article/details/99829118
        //https://www.cnblogs.com/newyouth/p/14014708.html
        httpRequest.headers().set(HttpHeaders.Names.HOST, "127.0.0.1");
        httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        httpRequest.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpRequest.content().readableBytes());
        httpRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");

        ctx.writeAndFlush(httpRequest);//flush
        System.out.println("客户端连接服务器，开始发送数据……完成");
    }

    // • 从服务器接收到数据后调用
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object response)
            throws Exception {
        System.out.println("client 读取server数据..");
        // 服务端返回消息后
        FullHttpResponse msg = (FullHttpResponse)response;
        String msgr = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("服务端数据为 :" + msgr);
        Rpcfx.RpcfxInvocationHandler.respJson = msgr;
        ctx.channel().close();
    }

    // • 发生异常时被调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("client exceptionCaught..");
        // 释放资源
        ctx.close();
    }
}