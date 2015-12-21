package com.ccn.zxing.zcode;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.ccn.zxing.zcode.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;


/**
 * 扫描
 * @author Administrator
 */
public class SweepActivity extends Activity implements Callback {

    private SweepActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;// surface有没有被绘制
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.90f;
    private boolean vibrate;// 完成扫描时是否震动提示
    private SweepView sweepView;
    // private ImageView returnButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sweepView = new SweepView(this);
        setContentView(sweepView);

        CameraManager.init(getApplication());
        /*
		 * returnButton = (ImageView) findViewById(R.id.iv_return_btn);
		 * returnButton.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { SweepActivity.this.finish();
		 * } });
		 */
//		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView = sweepView.getViewfinderView();
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);// activity静止一段时间会自动关闭
    }

    @Override
    protected void onResume() {
        super.onResume();
//		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceView surfaceView = sweepView.getSurfaceView();
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
//		initSysRingSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    // 初始化照相机
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new SweepActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    // 二维码扫描结果数据
    public void handleDecode(Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
        // viewfinderView.drawResultBitmap(barcode);//画结果图片
        playBeepSoundAndVibrate();// 启动声音效果

        String str = obj.getText();
        System.out.println("扫描结果：" + str);

        try {
            Intent intent = new Intent();
            intent.putExtra("ercode", str);
            setResult(1001, intent);
            finish();
        } catch (Exception e) {

        }
    }

    // 声音控制
    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.s);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    // 获取系统默认铃声的Uri
    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_NOTIFICATION);
    }

    private void initSysRingSound() {
        mediaPlayer = MediaPlayer.create(this, getSystemDefultRingtoneUri());
		/*不循环*/
        mediaPlayer.setLooping(false);
        try {
            mediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static final long VIBRATE_DURATION = 200L;

    // 启动声音功能
    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}