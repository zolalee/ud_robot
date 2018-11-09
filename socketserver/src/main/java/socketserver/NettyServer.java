package socketserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyServer {
    private static Handler handler;
    private static ServerBootstrap b = null;
    public static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();
    private static Context context;
    public NettyServer(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    //静态工厂方法
    public static NettyServer getInstance() {
        return classloader.sockServer;
    }
    private final static String TAG ="NettyServer";

    /**
     * 启动连接，配置参数，配置心跳参数、编码器、客户端拦截器
     */
    public void bind(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);//用于处理服务器端接收客户端连接
        NioEventLoopGroup workGroup = new NioEventLoopGroup(4);//进行网络通信（读写）
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();//辅助工具类，用于服务器通道的一系列配置
            bootstrap
                    .group(bossGroup, workGroup)//绑定两个线程组
                    .channel(NioServerSocketChannel.class)//指定NIO的模式
                    .childHandler(new ChannelInitializer<SocketChannel>() {//配置具体的数据处理方式
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new IdleStateHandler(300, 0, 0));//配置心跳机制参数
                            p.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            p.addLast(new LengthFieldPrepender(4, false));
                            p.addLast(new ChunkedWriteHandler());
                            p.addLast(new StringEncoder());
                            p.addLast(new MyServerHandler(handler,context));
                        }
                    });
            Channel ch = bootstrap.bind(port).sync().channel();
            Log.d(TAG, "------------------NettyServer Start------------------");
            ch.closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public static Map<String, Channel> getMap() {
        return map;

    }

    public static void setMap(Map<String, Channel> map) {
        NettyServer.map = map;
    }

    private static class classloader {
        private static final NettyServer sockServer = new NettyServer(handler,context);
    }
}
