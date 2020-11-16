package io.github.kimmking.gateway.outbound.httpclient5;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http5OutboundHandler {
    
    private String backendUrl;
    
    public Http5OutboundHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) throws IOException {
        String responseBody = "";
//        System.out.println(fullRequest);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet("http://127.0.0.1:8088/api/hello");
            for(String key: (fullRequest.headers().names())){
                if(key.equals("content-length")){
                    continue;
                }
                String value = fullRequest.headers().get(key);
                httpget.setHeader(key,value);
//                System.out.println(key+":"+value);
            }
            httpget.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<String>() {
                @Override
                public String handleResponse(ClassicHttpResponse response) throws IOException {
                    int status = response.getCode();
                    if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                        final HttpEntity entity = response.getEntity();
                        byte[] body = EntityUtils.toByteArray(entity);
                        DefaultFullHttpResponse fhresponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
                        fhresponse.headers().set("Content-Type", "application/json");
                        fhresponse.headers().setInt("Content-Length", Integer.parseInt(response.getFirstHeader("Content-Length").getValue()));
                        if (fullRequest != null) {
                            if (!HttpUtil.isKeepAlive(fullRequest)) {
                                ctx.write(fhresponse).addListener(ChannelFutureListener.CLOSE);
                            } else {
                                ctx.write(fhresponse);
                            }
                        }
                        ctx.flush();
                    } else {
                        exceptionCaught(ctx,null);
                    }
                    return null;
                }
            };
            responseBody = httpclient.execute(httpget,responseHandler);
        }
    }
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
