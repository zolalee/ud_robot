package video;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioRecord;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import example.x264sdk;

public class VideoService extends Service {
    private final static int ID_RTMP_PUSH_START = 100;
    private final static int ID_RTMP_PUSH_EXIT = 101;
    private final int WIDTH_DEF = 640;
    private final int HEIGHT_DEF = 480;
    private final int FRAMERATE_DEF = 3;
    private final int BITRATE_DEF = 800 * 1000;

    private final int SAMPLE_RATE_DEF = 22050;
    private final int CHANNEL_NUMBER_DEF = 2;

    private final static String TAG = "VideoService";
    private final boolean DEBUG_ENABLE = false; //音视频是否保存在本地

    private String _rtmpUrl = "rtmp://192.168.1.106/live/12345678";

    //PowerManager.WakeLock _wakeLock;
    private DataOutputStream _outputStream = null;

    private AudioRecord _AudioRecorder = null;
    private byte[] _RecorderBuffer = null;
    // private FdkAacEncode _fdkaacEnc = null;
    private int _fdkaacHandle = 0;

    public SurfaceView _mSurfaceView = null;
    private Camera _mCamera = null;
    private boolean _bIsFront = true;
    //private SWVideoEncoder _swEncH264 = null;
    private int _iDegrees = 0;

    private int _iRecorderBufferSize = 0;

    private Button _SwitchCameraBtn = null;

    private boolean _bStartFlag = false;

    private int _iCameraCodecType = android.graphics.ImageFormat.NV21;

    private byte[] _yuvNV21 = new byte[WIDTH_DEF * HEIGHT_DEF * 3 / 2];
    private byte[] _yuvEdit = new byte[WIDTH_DEF * HEIGHT_DEF * 3 / 2];

    //private RtmpSessionManager _rtmpSessionMgr = null;

    private Queue<byte[]> _YUVQueue = new LinkedList<byte[]>();
    private Lock _yuvQueueLock = new ReentrantLock();
    private ImageView mTitleBack;
    private Thread _h264EncoderThread = null;
    private x264sdk x264;

    private int fps = 20;

    private int bitrate = 90000;
    private int timespan = 90000 / fps;

    private long time;
    private Thread _AacEncoderThread = null;

    private int getDispalyRotation() {
        return 0;
    }

    private int getDisplayOritation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind");
        return null;
    }

    private Camera.PreviewCallback _previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] YUV, Camera currentCamera) {
            time += timespan;
            byte[] yuv420 =new byte[HEIGHT_DEF*WIDTH_DEF*3/2];
            YUV420SP2YUV420(YUV,yuv420,WIDTH_DEF,HEIGHT_DEF);
            x264.PushOriStream(yuv420, yuv420.length, time);
            Log.i("test1", "YUV size is " + YUV.length + "yuv420 is " + yuv420.length + " time is " + time);
        }
    };

    public void InitCamera() {
        Log.i(TAG,"InitCamera");
        _mCamera = Camera.open();
        Camera.Parameters parameters = _mCamera.getParameters();

        Size prevewSize = parameters.getPreviewSize();
        showlog("Original Width:" + prevewSize.width + ", height:" + prevewSize.height);

        List<Size> PreviewSizeList = parameters.getSupportedPreviewSizes();
        List<Integer> PreviewFormats = parameters.getSupportedPreviewFormats();
        showlog("Listing all supported preview sizes");
        for (Size size : PreviewSizeList) {
            showlog("  w: " + size.width + ", h: " + size.height);
        }
        List<Integer> supportedPreviewFrameRates = parameters
                .getSupportedPreviewFrameRates();
        for (int t: supportedPreviewFrameRates ) {
            showlog("supportedPreviewFrameRates is " + t);

        }
        Integer iNV21Flag = 0;
        Integer iYV12Flag = 0;
        for (Integer yuvFormat : PreviewFormats) {
            showlog("preview formats:" + yuvFormat);
            if (yuvFormat == android.graphics.ImageFormat.YV12) {
                iYV12Flag = android.graphics.ImageFormat.YV12;
            }
            if (yuvFormat == android.graphics.ImageFormat.NV21) {
                iNV21Flag = android.graphics.ImageFormat.NV21;
            }
        }

        if (iNV21Flag != 0) {
            _iCameraCodecType = iNV21Flag;
        } else if (iYV12Flag != 0) {
            _iCameraCodecType = iYV12Flag;
        }
        showlog("_iCameraCodecType is " + _iCameraCodecType);
        parameters.setPreviewSize(WIDTH_DEF, HEIGHT_DEF);
        parameters.setPreviewFormat(_iCameraCodecType);
        parameters.setPreviewFrameRate(fps);
        showlog("_iDegrees=" + _iDegrees);
        _mCamera.setDisplayOrientation(_iDegrees);
        parameters.setRotation(_iDegrees);
        _mCamera.setPreviewCallback(_previewCallback);
        _mCamera.setParameters(parameters);
       /* try {
            _mCamera.setPreviewDisplay(_mSurfaceView.getHolder());
        } catch (Exception e) {
            return;
        }*/
        SurfaceTexture st = new SurfaceTexture(0);
        try {
            _mCamera.setPreviewTexture(st);
            _mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void InitAll() {

       /* _mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceViewEx);
        _mSurfaceView.getHolder().setFixedSize(WIDTH_DEF, HEIGHT_DEF);
        _mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        _mSurfaceView.getHolder().setKeepScreenOn(true);
        _mSurfaceView.getHolder().addCallback(new SurceCallBack());

        //InitAudioRecord();

        _SwitchCameraBtn = (Button) findViewById(R.id.SwitchCamerabutton);
        _SwitchCameraBtn.setOnClickListener(_switchCameraOnClickedEvent);
         mTitleBack = (ImageView) findViewById(R.id.common_title_back);
         mTitleBack.setOnClickListener(this);*/
        x264 = new x264sdk(l);
        x264.initX264Encode(WIDTH_DEF, HEIGHT_DEF, fps, bitrate);
        InitCamera();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        InitAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        if (_mCamera!=null){
            _mCamera.setPreviewCallback(null);
            _mCamera.stopPreview();
            _mCamera.release();
            _mCamera = null;
        }
    }

    public void showlog(String info) {
        System.out.print("Watson " + info + "\n");
    }

    /*private void sendConMsg(byte[] videoData) {
        Map<String, Channel> clientChannel = NettyAudioServer.map;
        for (String clientIp : clientChannel.keySet()) {
            Log.i(TAG, clientIp);
            Channel channel = clientChannel.get(clientIp);//得到每个key多对用value的值
            Log.i(TAG, "clientIp is " + clientIp);
            Log.i(TAG, "videoData is " + videoData.length);
            Log.i(TAG, "videoData sleep is " + videoData.length);
            ByteBuf resp = Unpooled.copiedBuffer(videoData);
            channel.writeAndFlush(resp);
            Unpooled.unreleasableBuffer(resp);

        }
    }*/
    private void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height)
    {
        if (yuv420sp == null ||yuv420 == null)return;
        int framesize = width*height;
        int i = 0, j = 0;
        //copy y
        for (i = 0; i < framesize; i++)
        {
            yuv420[i] = yuv420sp[i];
        }
        i = 0;
        for (j = 0; j < framesize/2; j+=2)
        {
            yuv420[i + framesize*5/4] = yuv420sp[j+framesize];
            i++;
        }
        i = 0;
        for(j = 1; j < framesize/2;j+=2)
        {
            yuv420[i+framesize] = yuv420sp[j+framesize];
            i++;
        }
    }
    private x264sdk.listener l = new x264sdk.listener(){

        @Override
        public void h264data(byte[] buffer, int length) {
            Log.i("hm " ,"buffer is " + buffer.length);
            //sendConMsg(buffer);
        }
    };
    private byte[] _yuvTmp = null;
    public void swapYV12toI420_Ex(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
 /*    	final int iSize = width*height;

     	long start = System.currentTimeMillis();
    	byte bTmp = 0;
		for (int i = iSize; i < iSize+iSize/4; i += 1) {
			bTmp = yv12bytes[i+1];
			yv12bytes[i+1] = yv12bytes[i+iSize/4];
			yv12bytes[i+iSize/4] = bTmp;
		}
		long cha1 = System.currentTimeMillis()-start;
		System.arraycopy(yv12bytes, 0, i420bytes, 0, yv12bytes.length);
		long cha2 = System.currentTimeMillis()-start;
		Log.e("EX..shijiancha==", ""+(int)cha1+""+cha2);
*/
        int iSize = width*height;
//		byte[] yuvTmp = new byte[width*height*1/2];
        System.arraycopy(yv12bytes, iSize, _yuvTmp, 0, iSize/4);//U-->tmp

        System.arraycopy(yv12bytes, 0, i420bytes, 0, iSize);//Y
        System.arraycopy(yv12bytes, iSize+iSize/4, i420bytes, iSize,iSize/4);//U
        System.arraycopy(_yuvTmp, 0, i420bytes, iSize+iSize/4, iSize/4);//V


//        for (int i = 0; i < width*height; i++)
//            i420bytes[i] = yv12bytes[i];
//        for (int i = width*height; i < width*height + (width/2*height/2); i++)
//            i420bytes[i] = yv12bytes[i + (width/2*height/2)];
//        for (int i = width*height + (width/2*height/2); i < width*height + 2*(width/2*height/2); i++)
//            i420bytes[i] = yv12bytes[i - (width/2*height/2)];
    }
}
