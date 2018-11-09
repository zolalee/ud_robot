package socketserver;

import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.util.Log;

import com.cloudminds.socketserver.utils.ActionUtil;
import com.cloudminds.socketserver.utils.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NettyUtil {
    private final static String TAG = "NettyUtil";
    private static boolean isUpLoad = false;
    private static ByteArrayOutputStream baos;
    private static Thread thread;
    private static final Handler mHandler = new Handler();
    private static Runnable runnable;
    private static byte[] ba;
    public static boolean sendBatteryMsg(String battery) {
        Map<String, Channel> clientChannel = NettyServer.map;
        boolean isSuccess = false;
        if (clientChannel.size() == 0){
            Log.e(TAG,"sendBatteryMsg, clientChannel is null");
            return false;
        }
        for (String clientIp : clientChannel.keySet()) {
            Log.i(TAG, clientIp);
            Channel channel = clientChannel.get(clientIp);//得到每个key多对用value的值
            Log.i(TAG, "sendBatteryMsg,clientIp is " + clientIp);
            Log.i(TAG, "sendBatteryMsg,battery is " + battery);
            String batteryMsg = "commandStr:" + ActionUtil.BATTERY_INFO + ";msgStr:" + battery;
            ByteBuf resp = Unpooled.copiedBuffer(batteryMsg.getBytes());
            isSuccess = channel.isActive();
            if (isSuccess){
                channel.writeAndFlush(resp).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        channelFuture.isSuccess();
                        Log.i(TAG, "sendBatteryMsg operationComplete is " + channelFuture.isSuccess());
                    }
                });
            }
            Log.i(TAG, "sendBatteryMsg,isSuccess is " + isSuccess);
        }
        return isSuccess;

    }
    public static boolean sendBatteryMsgForLocal(String battery) {
        Map<String, Channel> clientChannel = NettyServer.map;
        boolean isSuccess = false;
        if (clientChannel.size() == 0){
            Log.e(TAG,"sendBatteryMsgForLocal, clientChannel is null");
            return false;
        }
        for (String clientIp : clientChannel.keySet()) {
            Log.i(TAG, clientIp);
            Channel channel = clientChannel.get(clientIp);//得到每个key多对用value的值
            Log.i(TAG, "sendBatteryMsgForLocal,clientIp is " + clientIp);
            Log.i(TAG, "sendBatteryMsgForLocal,battery is " + battery);
            String batteryMsg = "commandStr:" + ActionUtil.BATTERY_INFO + ";msgStr:" + battery;
            ByteBuf resp = Unpooled.copiedBuffer(batteryMsg.getBytes());
            isSuccess = channel.isActive();
            if (isSuccess){
                channel.writeAndFlush(resp).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        channelFuture.isSuccess();
                        Log.i(TAG, "sendBatteryMsgForLocal operationComplete is " + channelFuture.isSuccess());
                    }
                });
            }
            Log.i(TAG, "sendBatteryMsgForLocal,isSuccess is " + isSuccess);
        }
        return isSuccess;

    }
    public static boolean saveBatteryInfoToSP(SharedPreferences sp,String level) {
        if (sp != null){
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constant.BATTERY_CAPACITY, level);
            return editor.commit();
        }
        return false;

    }
    public static void sendConMsg(byte[] audioData ) {
        Map<String, Channel> clientChannel = NettyAudioServer.map;
        for (String clientIp : clientChannel.keySet()) {
            Log.i(TAG, clientIp);
            Channel channel = clientChannel.get(clientIp);//得到每个key多对用value的值
            Log.i(TAG, "clientIp is " + clientIp);
            Log.i(TAG, "audiodata is " + audioData.length);
            ByteBuf resp = Unpooled.copiedBuffer(audioData);
            channel.writeAndFlush(resp);

        }
    }

    public static void sendAudioConMsg(byte[] audioData ) {
        Map<String, Channel> clientChannel = NettyAudioServer.map;
        for (String clientIp : clientChannel.keySet()) {
            Log.i(TAG, clientIp);
            Channel channel = clientChannel.get(clientIp);//得到每个key多对用value的值
            Log.i(TAG, "sendAudioConMsg,clientIp is " + clientIp);
            Log.i(TAG, "sendAudioConMsg,audiodata is " + audioData.length);
            ByteBuf resp = Unpooled.copiedBuffer(audioData);
            channel.writeAndFlush(resp);

        }
    }

    public static void saveYUVtoPicture(byte[] data,int width,int height) throws IOException {
        ByteArrayOutputStream outStream = null;
        Log.i(TAG,"saveYUVtoPicture,isUpLoad is " + isUpLoad);
        if (isUpLoad){
            try {
                ba = data;
              /*  YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0,width, height), 80, baos);
                Log.i(TAG,"baos is " + baos.toByteArray().length);*/
            }catch (Exception e){

            }
        }
    }
    public static void sendPictureAcqMsg() {
        isUpLoad = true;
    }
    public static void sendPictureStopMsg() {
        isUpLoad = false;
    }
    public static void initTimer(){
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"run----");
                // TODO Auto-generated method stub
                NettyUtil.sendConMsg(ba);
                if (isUpLoad){
                    mHandler.postDelayed(this, 1500);
                }
            }
        };
        mHandler.postDelayed(runnable, 1500);
    }
    public static void stopTimer(){
        mHandler.removeCallbacks(runnable);
    }
}
