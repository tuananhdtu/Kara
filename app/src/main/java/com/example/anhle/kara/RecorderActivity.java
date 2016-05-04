package com.example.anhle.kara;

/**
 * Created by anhle on 4/26/16.
 */
        import java.io.File;
        import java.io.IOException;
        import java.nio.ByteBuffer;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.Comparator;
        import java.util.List;

        import android.annotation.SuppressLint;
        import android.app.AlertDialog;
        import android.app.ProgressDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.res.Resources;
        import android.graphics.Color;
        import android.graphics.PixelFormat;
        import android.hardware.Camera;
        import android.hardware.Camera.Size;
        import android.media.CamcorderProfile;
        import android.media.MediaCodec;
        import android.media.MediaExtractor;
        import android.media.MediaFormat;
        import android.media.MediaMuxer;
        import android.media.MediaPlayer;
        import android.media.MediaRecorder;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
        import android.os.Message;
        import android.text.Spannable;
        import android.text.SpannableString;
        import android.text.style.ForegroundColorSpan;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

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

    private ProgressView progressView;

    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    private File mOutputFile;

    private boolean mIsRecording = false;

    private int positionMusic;
    private Resources mResources;
    private String mPackageName;

    private List<Size> mSupportVideoSizes;
    ArrayList<Lyric> lyrics;
    MediaPlayer player;
    LyricView mLyricView;
    TextView mLyricCurrentView;
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

        progressView = (ProgressView) findViewById(R.id.progressView);
        progressView.setWidth(getWindowManager().getDefaultDisplay().getWidth());
        progressView.setPosition(0);
        mLyricView = (LyricView) findViewById(R.id.lyricView);
         mLyricCurrentView = (TextView) findViewById(R.id.lyricCurrentView);

        new JSONParse().execute();

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
    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RecorderActivity.this);
            pDialog.setMessage("Lấy dữ liệu ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            JSONObject json = null;
            // Getting JSON from URL
            json = jParser.getJSONFromUrl("http://www.ikara.co/test/getlyric?lyrickey=aglzfmlrYXJhNG1yDAsSBUx5cmljGNdaDA");

            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            try {
                JSONArray lines = json.getJSONArray("lines");
                lyrics = LyricUtils.parseLyric(lines);

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void exit(final int resultCode, final Intent data) {
        if(mIsRecording) {
            new AlertDialog.Builder(RecorderActivity.this)
                    .setTitle("Thông báo")
                    .setMessage("Đang thu âm, bạn có muốn hủy không？")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {

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
                    .setNegativeButton("Không", new DialogInterface.OnClickListener() {

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
                case Config.ID_TIME_COUNT:
                    if(mIsRecording) {
                        if(msg.arg1 > msg.arg2) {
                            mTvTimeCount.setText("00:00");
                            stopRecord();
                        } else {
                            positionMusic =  msg.arg1;
                            progressView.setPosition(positionMusic);
                            progressView.invalidate();

                            int seconds = msg.arg1 % 60;
                            int minutes = (msg.arg1 / 60) % 60;
                            mTvTimeCount.setText(String.format("%02d : %02d", minutes, seconds));
                            Message msg2 = mHandler.obtainMessage(Config.ID_TIME_COUNT, msg.arg1 + 1, msg.arg2);
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
                mLyricView.stop();
                stopRecord();
                player.stop();
                player = null;
            } else {
                playMusic();
            }
        }
    };
    Handler handler ;
    private long startTime, currentTime, finishedTime = 0L;
    private int duration = 22000 / 4;
    private int endTime = 0;
    private int mLyricIndex = 0;
    private int mLyricIndexChild = 0;
    private void playMusic() {
        try {
            player = new MediaPlayer();

            player.setDataSource("http://data2.ikara.co/data/karaokes/all/8.mp3");
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    final int HOUR = 60*60*1000;
                    final int MINUTE = 60 * 1000;
                    final int SECOND = 1000;

                    int durationInMillis = player.getDuration();

                    int durationMint = player.getDuration()/1000;

                    progressView.setDuration(player.getDuration()/1000);

                    player.start();
                    mLyricView.setLyric(lyrics);
                    mLyricView.play();
                    startRecord();

                    handler = new Handler();
                    startTime = Long.valueOf(System.currentTimeMillis());
                    currentTime = startTime;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            currentTime = Long.valueOf(System.currentTimeMillis());
                            finishedTime = Long.valueOf(currentTime)
                                    - Long.valueOf(startTime);
                            mLyricIndex = LyricUtils.getSentenceIndex(lyrics, finishedTime, mLyricIndex, 0);
                            if (finishedTime >= duration + 30) {
                                Toast.makeText(RecorderActivity.this, "Move to next screen",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                endTime = (int) (finishedTime / 250);// divide this by
                                // 1000,500,250,125
                                Spannable spannableString = new SpannableString(lyrics.get(mLyricIndex+1).showWord());
                                spannableString.setSpan(new ForegroundColorSpan(
                                                Color.YELLOW), 0, endTime,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                mLyricCurrentView.setText(spannableString);
                                handler.postDelayed(this, 10);
                            }
                        }
                    }, 10);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
            if (mOutputFile != null && !StringUtil.isEmpty(mOutputFile.getAbsolutePath())) {
                intent.putExtra(Config.RESULT_DATA, mOutputFile.getAbsolutePath());
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
            if (initVideoRecorder()) {
                mMediaRecorder.start();
                mButton.setBackgroundResource(mResources.getIdentifier("yuninfo_btn_video_stop", "drawable", mPackageName));
            } else {
                releaseMediaRecorder();
                mButton.setBackgroundResource(mResources.getIdentifier("yuninfo_btn_video_start", "drawable", mPackageName));
            }
            mTvTimeCount.setVisibility(View.VISIBLE);
            Message msg = mHandler.obtainMessage(Config.ID_TIME_COUNT, 1, Config.MAX_VIDEO_DURATION / 1000);
            mHandler.sendMessage(msg);
            mIsRecording = true;
        } catch (Exception e) {
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

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
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