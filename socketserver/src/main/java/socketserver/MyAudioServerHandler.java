package socketserver;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cloudminds.socketserver.utils.ActionUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class MyAudioServerHandler extends ChannelInboundHandlerAdapter {
    private Handler handler;
    private RandomAccessFile raf = null;
    private FileOutputStream fos;
    private BufferedOutputStream bufferedOutputStream;
    private final static String TAG = "MyAudioServerHandler";
    private String commandStr;
    private String responseMsg;
    private Message msgServer;

    public MyAudioServerHandler(Handler handler) {
        this.handler = handler;
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, "channelActive Client connect:" + ctx.channel().localAddress().toString() + " "+ ctx.channel().id().toString());
        NettyAudioServer.getMap().put(getIPString(ctx), ctx.channel());

    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, ctx.channel().localAddress().toString() + " channelInactive");
        // 关闭流
        if (bufferedOutputStream != null) {
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        }
        NettyAudioServer.getMap().remove(getIPString(ctx));
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()]; // 获取缓冲区可读的字节数
        buf.readBytes(bytes);

        String body = new String(bytes, "UTF-8");
        Log.d(TAG, "body is: " + body);

        String msgStr[] = body.split(";");
        commandStr = msgStr[0].split(":")[1];
        Log.d(TAG, "commandStr is " + commandStr);
        switch (commandStr) {
            case ActionUtil.PICTURE_RES:
                responseMsg = "commandStr:"+ ActionUtil.PICTURE_RES +";Msg:" + "upload";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.PICTURE_STOP:
                responseMsg = "commandStr:"+ ActionUtil.PICTURE_STOP +";Msg:" + "stop";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            default:
                break;

        }

    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Log.d(TAG, "Error:" + cause.getMessage());
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        Log.d(TAG, "Error:" + "---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {

        Log.d(TAG, "---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {

        Log.d(TAG, "---ALL_IDLE---");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        }
    }

    public static String getIPString(ChannelHandlerContext ctx){
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        Log.i(TAG,"getIPString ipString is " + ipString);
        return ipString;
    }

}