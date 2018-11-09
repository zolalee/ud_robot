package socketserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;


import com.cloudminds.socketserver.utils.ActionUtil;
import com.cloudminds.socketserver.utils.Constant;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class MyServerHandler extends ChannelInboundHandlerAdapter {
    private Handler handler;
    private RandomAccessFile raf = null;
    private boolean msgSend = true;
    private FileOutputStream fos;
    private BufferedOutputStream bufferedOutputStream;
    private long fileLength; // 文件长度
    private String fileName;
    private String md5;
    private String savaPath;
    private String commandStr;
    private String responseMsg;
    private Message msgServer;
    private final static String TAG = "MyServerHandler";
    private Context context;
    private SharedPreferences sp;

    public MyServerHandler(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        msgSend = true;
        Log.d(TAG, "channelActive Client connect:" + ctx.channel().localAddress().toString() + " "+ ctx.channel().id().toString());
        NettyServer.getMap().put(getIPString(ctx), ctx.channel());
        if (context != null){
            sp = context.getSharedPreferences(Constant.BATTERY_INFO, Context.MODE_PRIVATE);
            String batteryInfo = sp.getString(Constant.BATTERY_CAPACITY,"");
            Log.i(TAG,"channelActive,batteryInfo is " + batteryInfo);
            if (!TextUtils.isEmpty(batteryInfo)){
                NettyUtil.sendBatteryMsgForLocal(batteryInfo);
            }
        }

    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, ctx.channel().localAddress().toString() + " channelInactive");
        // 关闭流
        if (bufferedOutputStream != null) {
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        }
        msgSend = true;
        NettyServer.getMap().remove(getIPString(ctx));

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
            case ActionUtil.NOD_HEAD_ACTION:
                responseMsg = "commandStr:RobotMotion_NodHead;Msg:" + "angle,30";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.WAVE_ACTION:
                responseMsg = "commandStr:" + ActionUtil.WAVE_ACTION + ";Msg:" + "angle,30";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.CAMERA_ACTION:
                responseMsg = "commandStr:" + ActionUtil.CAMERA_ACTION + ";Msg:" + "angle,30";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.SPEAK_ACTION:
                String speakInfo = msgStr[1].split(":")[1];
                Log.i(TAG, "speakInfo is " + speakInfo);
                responseMsg = "commandStr:" + ActionUtil.SPEAK_ACTION + ";Msg:" + speakInfo;
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.START_RECORD_ACTION:
                Log.i(TAG, "START_RECORD_ACTION");
                responseMsg = "commandStr:" + ActionUtil.START_RECORD_ACTION + ";Msg:" + "start";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.STOP_RECORD_ACTION:
                Log.i(TAG, "STOP_RECORD_ACTION");
                responseMsg = "commandStr:" + ActionUtil.STOP_RECORD_ACTION + ";Msg:" + "stop";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_SMILE:
                Log.i(TAG, "ACTION_SMILE");
                responseMsg = "commandStr:" + ActionUtil.ACTION_SMILE + ";Msg:" + "smile";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_SAD:
                Log.i(TAG, "ACTION_SAD");
                responseMsg = "commandStr:" + ActionUtil.ACTION_SAD + ";Msg:" + "sad";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_FORWARD:
                Log.i(TAG, "ACTION_FORWARD");
                responseMsg = "commandStr:" + ActionUtil.ACTION_FORWARD + ";Msg:" + "forward";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_BACK:
                Log.i(TAG, "ACTION_BACK");
                responseMsg = "commandStr:" + ActionUtil.ACTION_BACK + ";Msg:" + "back";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_WAVE:
                Log.i(TAG, "ACTION_WAVE");
                responseMsg = "commandStr:" + ActionUtil.ACTION_WAVE + ";Msg:" + "wave";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_CLAP:
                Log.i(TAG, "ACTION_CLAP");
                responseMsg = "commandStr:" + ActionUtil.ACTION_CLAP + ";Msg:" + "clap";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_THINKS:
                Log.i(TAG, "ACTION_THINKS");
                responseMsg = "commandStr:" + ActionUtil.ACTION_THINKS + ";Msg:" + "think";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_FLYKISS:
                Log.i(TAG, "ACTION_FLYKISS");
                responseMsg = "commandStr:" + ActionUtil.ACTION_FLYKISS + ";Msg:" + "kiss";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_TURNLEFT:
                Log.i(TAG, "ACTION_TURNLEFT");
                responseMsg = "commandStr:" + ActionUtil.ACTION_TURNLEFT + ";Msg:" + "left";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_TURNRIGHT:
                Log.i(TAG, "ACTION_TURNRIGHT");
                responseMsg = "commandStr:" + ActionUtil.ACTION_TURNRIGHT + ";Msg:" + "right";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_SLEEP:
                Log.i(TAG, "ACTION_SLEEP");
                responseMsg = "commandStr:" + ActionUtil.ACTION_SLEEP + ";Msg:" + "sleep";
                msgServer = new Message();
                msgServer.obj = responseMsg;
                handler.sendMessage(msgServer);
                break;
            case ActionUtil.ACTION_WAKE:
                Log.i(TAG, "ACTION_WAKE");
                responseMsg = "commandStr:" + ActionUtil.ACTION_WAKE + ";Msg:" + "wake";
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
        Log.d(TAG,"Error:" + cause.getMessage());
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        Log.d(TAG,"Error:" + "---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {

        Log.d(TAG, "---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {

        Log.d(TAG,"---ALL_IDLE---");
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