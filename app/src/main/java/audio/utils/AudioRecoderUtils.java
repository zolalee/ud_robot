package audio.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import socketserver.NettyUtil;

/**
 * 录音
 * Created by fan on 2016/6/23.
 */
public class AudioRecoderUtils {

    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 16000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状态
    private final static String TAG = "AudioRecoderUtils";
    private byte[] audioData;
    private static AudioRecoderUtils audioRecoderUtils;
    public void creatAudioRecord() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        Log.d("TestAudioRecord", "the bufferSizeInBytes size is " + bufferSizeInBytes);
        // 创建AudioRecord对象
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        audioData = new byte[bufferSizeInBytes];

    }

    public static AudioRecoderUtils getInstance() {
        if (audioRecoderUtils ==null){
            audioRecoderUtils = new AudioRecoderUtils();
        }
        return audioRecoderUtils;
    }

    public void startRecord() {
        Log.i(TAG, "startRecord");
        creatAudioRecord();
        audioRecord.startRecording();
        // 让录制状态为true
        isRecord = true;
        // 开启音频文件写入线程,发送到chanel上
        new Thread(new AudioRecoderUtils.AudioRecordThread()).start();
    }

    private void pauseRecord() {
        isRecord = false;
    }

    public void stopRecord() {
        Log.i(TAG, "stopRecord");
        close();
    }

    private void close() {
        if (audioRecord != null) {
            Log.i(TAG, "close");
            isRecord = false;//停止文件写入
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDataToChanel();
        }

    }

    private void writeDataToChanel() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        int readsize = 0;
        while (isRecord == true) {
            readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            //Log.i(TAG, "writeDataToChanel,readsize is " + readsize);
            NettyUtil.sendAudioConMsg(audioData);
        }

    }

    /*private void sendConMsg(byte[] audioData ) {
        Map<String, Channel> clientChannel = NettyAudioServer.map;
        for (String clientIp : clientChannel.keySet()) {
            Log.e(TAG, clientIp);
            Channel channel = clientChannel.get(clientIp);//得到每个key多对用value的值
            Log.i(TAG, "clientIp is " + clientIp);
            Log.i(TAG, "audiodata is " + audioData.length);
            ByteBuf resp = Unpooled.copiedBuffer(audioData);
            channel.writeAndFlush(resp);

        }
    }*/
}
