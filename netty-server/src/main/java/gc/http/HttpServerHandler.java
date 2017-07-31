package gc.http;

import com.google.common.cache.Cache;
import com.google.common.io.Resources;

import gc.kafka.producer.Producer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;


public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Cache<Long, String> dataCache;
    private HttpRequest request;
    private final StringBuilder buf = new StringBuilder();

    public HttpServerHandler(Cache<Long, String> dataCache) {
        this.dataCache = dataCache;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
    	System.out.println(msg.uri()+"----------->"+msg.getMethod());
    	
        try {
        	
        	 HttpRequest request = this.request = (HttpRequest) msg;
        	if(msg.getMethod().equals(HttpMethod.POST) || msg.getMethod().equals(HttpMethod.GET) || msg.getMethod().equals(HttpMethod.OPTIONS)) {
 
        		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
                Map<String, List<String>> params = queryStringDecoder.parameters();
                if (!params.isEmpty()) {
                    for (Entry<String, List<String>> p: params.entrySet()) {
                        String key = p.getKey();
                        List<String> vals = p.getValue();
                        for (String val : vals) {
                        	System.out.println("PARAM: ---------->"+key+" : "+val);
                        	new Producer().pushJSON(val);
                        }
                    }
                  
                }
            }
        	System.out.println("after: ---------->");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (msg.getUri().equals("/")) {
            serveStatic(ctx, "/index.html");
        } else if (msg.getUri().contains("/data")) {
            serveData(ctx);
        } else {
           serveStatic(ctx, msg.getUri());
        }
    }

    private void serveData(ChannelHandlerContext ctx) {
        StringBuilder sb = new StringBuilder();
        JSONObject obj = new JSONObject();
        obj.put("Message", "Data Updated Successfully");
        sb.append(obj.toString());
        ByteBuf content = Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        ctx.write(response);
    }

    private void serveStatic(ChannelHandlerContext ctx, String path) throws Exception {
        try {
            byte[] raw = Resources.toByteArray(Resources.getResource(path.substring(1)));
            ByteBuf content = Unpooled.wrappedBuffer(raw);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
            ctx.write(response);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            serve404(ctx);
        }

    }

    private void serve404(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, 0);
        ctx.write(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
