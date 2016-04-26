package com.example.anhle.kara;

/**
 * Created by anhle on 4/26/16.
 */

        import java.io.File;
        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.Comparator;
        import java.util.List;

        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.res.Resources;
        import android.graphics.PixelFormat;
        import android.hardware.Camera;
        import android.hardware.Camera.Size;
        import android.media.CamcorderProfile;
        import android.media.MediaRecorder;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;

        import com.example.anhle.kara.DateUtil;
        import com.example.anhle.kara.LogUtil;
        import com.example.anhle.kara.StringUtil;

/**
 * 作用：
 *
 * @author yinglovezhuzhu@gmail.com
 */
public class RecorderActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = RecorderActivity.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private ImageButton mIbtnCancel;
    private ImageButton mIbtnOk;
    private Button mButton;
    private TextView mTvTimeCount;

    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    private File mOutputFile;

    private boolean mIsRecording = false;


    private Resources mResources;
    private String mPackageName;

    private List<Size> mSupportVideoSizes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResources = getResources();
        mPackageName = getPackageName();


        int layoutId = mResources.getIdentifier("yuninfo_activity_video_recorder", "layout", mPackageName);
        setContentView(layoutId);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        initView();
    }

    @SuppressWarnings("deprecation")
    private void initView() {

        mSurfaceView = (SurfaceView) findViewById(mResources.getIdentifier("yuninfo_sv_recorder_preview", "id", mPackageName));
        mButton = (Button) findViewById(mResources.getIdentifier("yuninfo_btn_video_record", "id", mPackageName));
        mIbtnCancel = (ImageButton) findViewById(mResources.getIdentifier("yuninfo_ibtn_video_cancel", "id", mPackageName));
        mIbtnOk = (ImageButton) findViewById(mResources.getIdentifier("yuninfo_ibtn_video_ok", "id", mPackageName));
        mIbtnCancel.setOnClickListener(mCancelListener);
        mIbtnOk.setOnClickListener(mOkListener);
        mIbtnCancel.setVisibility(View.INVISIBLE);
        mIbtnOk.setVisibility(View.INVISIBLE);
        mTvTimeCount = (TextView) findViewById(mResources.getIdentifier("yuninfo_tv_recorder_time_count", "id", mPackageName));
        mTvTimeCount.setVisibility(View.INVISIBLE);

        mButton.setBackgroundResource(mResources.getIdentifier("yuninfo_btn_video_start", "drawable", mPackageName));
        mButton.setOnClickListener(mBtnListener);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            try {
                mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
        }

    }


    private void exit(final int resultCode, final Intent data) {
        if(mIsRecording) {
            new AlertDialog.Builder(RecorderActivity.this)
                    .setTitle("提示")
                    .setMessage("正在录制视频，是否退出？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopRecord();
                            if(resultCode == RESULT_CANCELED) {
                                deleteFile(mOutputFile);
                            }
                            setResult(resultCode, data);
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    }).show();
            return;
        }
        if(resultCode == RESULT_CANCELED) {
            deleteFile(mOutputFile);
        }
        setResult(resultCode, data);
        finish();
    }

    private void deleteFile(File delFile) {
        if(delFile == null) {
            return;
        }
        final File file = new File(delFile.getAbsolutePath());
        delFile = null;
        new Thread() {
            @Override
            public void run() {
                super.run();
                if(file.exists()) {
                    file.delete();
                }
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Config.YUNINFO_ID_TIME_COUNT:
                    if(mIsRecording) {
                        if(msg.arg1 > msg.arg2) {
                            mTvTimeCount.setText("00:00");
                            stopRecord();
                        } else {
                            int seconds = msg.arg1 % 60;
                            int minutes = (msg.arg1 / 60) % 60;
                            mTvTimeCount.setText(String.format("%02d : %02d", minutes, seconds));
                            Message msg2 = mHandler.obtainMessage(Config.YUNINFO_ID_TIME_COUNT, msg.arg1 + 1, msg.arg2);
                            mHandler.sendMessageDelayed(msg2, 1000);
                        }
                    }
                    break;

                default:
                    break;
            }
        };

    };

    private View.OnClickListener mBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(mIsRecording) {
                stopRecord();
            } else {
                startRecord();
            }
        }
    };

    private View.OnClickListener mCancelListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            exit(RESULT_CANCELED, null);
        }
    };

    private View.OnClickListener mOkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecorderActivity.this, VideoPlayer.class);
            if(mOutputFile != null && !StringUtil.isEmpty(mOutputFile.getAbsolutePath())) {
                intent.putExtra(Config.YUNINFO_RESULT_DATA, mOutputFile.getPath());
            }
            startActivity(intent);
            finish();
        }
    };

    @SuppressLint("NewApi")
    private void openCamera() {
        //Open camera
        try {
            this.mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Camera.Parameters parameters = mCamera.getParameters();
            System.out.println(parameters.flatten());
            parameters.set("orientation", "portrait");
            mCamera.setParameters(parameters);
            mCamera.lock();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                try {
                    mCamera.setDisplayOrientation(90);
                } catch (NoSuchMethodError e) {
                    e.printStackTrace();
                }
            }
            mSupportVideoSizes = parameters.getSupportedVideoSizes();
            if(mSupportVideoSizes == null || mSupportVideoSizes.isEmpty()) {  //For some device can't get supported video size
                String videoSize = parameters.get("video-size");
                LogUtil.i(TAG, videoSize);
                mSupportVideoSizes = new ArrayList<Camera.Size>();
                if(!StringUtil.isEmpty(videoSize)) {
                    String [] size = videoSize.split("x");
                    if(size.length > 1) {
                        try {
                            int width = Integer.parseInt(size[0]);
                            int height = Integer.parseInt(size[1]);
                            mSupportVideoSizes.add(mCamera.new Size(width, height));
                        } catch (Exception e) {
                            LogUtil.e(TAG, e.toString());
                        }
                    }
                }
            }
            for (Size size : mSupportVideoSizes) {
                LogUtil.i(TAG, size.width + "<>" + size.height);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Open Camera error\n" + e.toString());
        }
    }

    @SuppressLint("NewApi")
    private boolean initVideoRecorder() {

        mCamera.unlock();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        LogUtil.i("Camera", mCamera);
        LogUtil.i("Camera", mMediaRecorder);
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        } catch (Exception e) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            try {
                CamcorderProfile lowProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                CamcorderProfile hightProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                if(lowProfile != null && hightProfile != null) {
                    int audioBitRate = lowProfile.audioBitRate > 128000 ? 128000 : lowProfile.audioBitRate;
                    lowProfile.audioBitRate = audioBitRate > hightProfile.audioBitRate ? hightProfile.audioBitRate : audioBitRate;
                    lowProfile.audioSampleRate = 48000 > hightProfile.audioSampleRate ? hightProfile.audioSampleRate : 48000;
//					lowProfile.duration = 20 > hightProfile.duration ? hightProfile.duration : 20;
//					lowProfile.videoFrameRate = 20 > hightProfile.videoFrameRate ? hightProfile.videoFrameRate : 20;
                    lowProfile.duration = hightProfile.duration;
                    lowProfile.videoFrameRate = hightProfile.videoFrameRate;
                    lowProfile.videoBitRate = 1500000 > hightProfile.videoBitRate ? hightProfile.videoBitRate : 1500000;;
                    if(mSupportVideoSizes != null && !mSupportVideoSizes.isEmpty()) {
                        int width = 640;
                        int height = 480;
                        Collections.sort(mSupportVideoSizes, new SizeComparator());
                        int lwd = mSupportVideoSizes.get(0).width;
                        for (Size size : mSupportVideoSizes) {
                            int wd = Math.abs(size.width - 640);
                            if(wd < lwd) {
                                width = size.width;
                                height = size.height;
                                lwd = wd;
                            } else {
                                break;
                            }
                        }
                        lowProfile.videoFrameWidth = width;
                        lowProfile.videoFrameHeight = height;
                    }
                    System.out.println(lowProfile.audioBitRate);
                    System.out.println(lowProfile.audioChannels);
                    System.out.println(lowProfile.audioCodec);
                    System.out.println(lowProfile.audioSampleRate);
                    System.out.println(lowProfile.duration);
                    System.out.println(lowProfile.fileFormat);
                    System.out.println(lowProfile.quality);
                    System.out.println(lowProfile.videoBitRate);
                    System.out.println(lowProfile.videoCodec);
                    System.out.println(lowProfile.videoFrameHeight);
                    System.out.println(lowProfile.videoFrameWidth);
                    System.out.println(lowProfile.videoFrameRate);

                    mMediaRecorder.setProfile(lowProfile);
                }
            } catch (Exception e) {
                try {
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                } catch (Exception ex) {
                    e.printStackTrace();
                }
                try {
                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                } catch (Exception ex) {
                    e.printStackTrace();
                }
                if(mSupportVideoSizes != null && !mSupportVideoSizes.isEmpty()) {
                    Collections.sort(mSupportVideoSizes, new SizeComparator());
                    Size size = mSupportVideoSizes.get(0);
                    try {
                        mMediaRecorder.setVideoSize(size.width, size.height);
                    } catch (Exception ex) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mMediaRecorder.setVideoSize(640, 480); // Its is not on android docs but
                        // it needs to be done. (640x480
                        // = VGA resolution)
                    } catch (Exception ex) {
                        e.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        } else {
            try {
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(mSupportVideoSizes != null && !mSupportVideoSizes.isEmpty()) {
                Collections.sort(mSupportVideoSizes, new SizeComparator());
                Size size = mSupportVideoSizes.get(0);
                try {
                    mMediaRecorder.setVideoSize(size.width, size.height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mMediaRecorder.setVideoSize(640, 480); // Its is not on android docs but
                    // it needs to be done. (640x480
                    // = VGA resolution)
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        // Step 4: Set output file
        mOutputFile = new File(Environment.getExternalStorageDirectory(), "Video_"
                + DateUtil.getSystemDate("yyyy_MM_dd_HHmmss") + ".mp4");
        mMediaRecorder.setOutputFile(mOutputFile.getAbsolutePath());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            try {
                mMediaRecorder.setOrientationHint(270);
            } catch (NoSuchMethodError e) {
                e.printStackTrace();
            }
        }


        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("VideoPreview", "IllegalStateException preparing MediaRecorder: "	+ e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("VideoPreview", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset(); // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    private void startRecord() {
        try {
            // initialize video camera
            if (initVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();
                mButton.setBackgroundResource(mResources.getIdentifier("yuninfo_btn_video_stop", "drawable", mPackageName));
//				mButton.setEnabled(false);
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
                mButton.setBackgroundResource(mResources.getIdentifier("yuninfo_btn_video_start", "drawable", mPackageName));
            }
            mTvTimeCount.setVisibility(View.VISIBLE);
            mTvTimeCount.setText("00:0" + (Config.YUNINFO_MAX_VIDEO_DURATION / 1000));
            Message msg = mHandler.obtainMessage(Config.YUNINFO_ID_TIME_COUNT, 1, Config.YUNINFO_MAX_VIDEO_DURATION / 1000);
            mHandler.sendMessage(msg);
            mIsRecording = true;
        } catch (Exception e) {
            showShortToast("该操作系统不支持此功能");
            e.printStackTrace();
            exit(RESULT_ERROR, null);
        }
    }


    private void stopRecord() {
        // stop recording and release camera
        try {
            mMediaRecorder.stop(); // stop the recording
        } catch (Exception e) {
            if(mOutputFile != null && mOutputFile.exists()) {
                mOutputFile.delete();
                mOutputFile = null;
            }
            LogUtil.e(TAG, e.toString());
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock(); // take camera access back from MediaRecorder
        mButton.setBackgroundResource(mResources.getIdentifier("yuninfo_btn_video_start", "drawable", mPackageName));
        mIsRecording = false;

        mButton.setVisibility(View.GONE);
        mIbtnCancel.setVisibility(View.VISIBLE);
        mIbtnOk.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception e) {
                LogUtil.e(TAG, "Error setting camera preview: " + e.toString());
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                LogUtil.e(TAG, "Error setting camera preview: " + e.toString());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            exit(RESULT_CANCELED, null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private class SizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return rhs.width - lhs.width;
        }
    }
}