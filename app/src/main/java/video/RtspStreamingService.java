package video;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import cloudminds.ud_robot.MainActivity;
import cloudminds.ud_robot.R;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

/**
 * A straightforward example of how to use the RTSP server included in libstreaming.
 */
public class RtspStreamingService extends Service {

    private final static String TAG = "RtspStreamingService";

    private SurfaceView mSurfaceView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private int screenWidth;
    private int screenHeight;
    private View mRecorderView;
    private Handler mHandler;
    private static final int NOTIFICATION_DI = 1234;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Background Video Recorder")
                .setContentText("Click into the application")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();
        startForeground(NOTIFICATION_DI, notification);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mRecorderView = LayoutInflater.from(this).inflate(R.layout.activity_rtsp_streaming, null);
        mSurfaceView = (SurfaceView) mRecorderView.findViewById(R.id.surface);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = 1;
        mLayoutParams.height = 1;
        mWindowManager.addView(mRecorderView, mLayoutParams);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
        editor.commit();
        // Configures the SessionBuilder
        SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(0)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setVideoEncoder(SessionBuilder.VIDEO_H264);

        // Starts the RTSP server
        this.startService(new Intent(this, RtspServer.class));
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_REDELIVER_INTENT;
    }
}
