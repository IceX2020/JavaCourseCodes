package io.github.kimmking.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class Http4InboundFilter implements HttpRequestFilter{
    private final String nameServer;

    public Http4InboundFilter(String nameServer) {
        this.nameServer = nameServer;
    }

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        fullRequest.headers().set("nio", "卡");
    }
}
