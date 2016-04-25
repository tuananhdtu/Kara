package com.example.anhle.kara;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by anhle on 4/25/16.
 */
public class VideoRecord extends Activity implements SurfaceHolder.Callback, VideoView {
    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    private MediaRecorder mMediaRecorder;
    private VideoPresenter presenter;
    private boolean mInitSuccesful;
    private boolean isRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        setContentView(R.layout.video_record);

        presenter = new VideoPresenter(this);

        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Button btnRecord = (Button) findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.clickButtonRecord();
            }
        });
    }
    public void setCameraDisplayOrientation()
    {
        if (mCamera == null)
        {
            return;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(1, info);

        WindowManager winManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int rotation = winManager.getDefaultDisplay().getRotation();

        int degrees = 0;

        switch (rotation)
        {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (Camera.getNumberOfCameras() >= 2){
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }else {
                mCamera = Camera.open();
            }
            setCameraDisplayOrientation();
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }
        Camera.Parameters params = mCamera.getParameters(); // mCamera is a Camera object
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        for (int j= 0 ; j< sizes.size(); j++){
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            if (width == (int) sizes.get(j).width){
                params.setPreviewSize((int) sizes.get(j).width, (int) sizes.get(j).height);
                mCamera.setParameters(params);

                ViewGroup.LayoutParams params1 = surfaceView.getLayoutParams();
                params1.height = (int) sizes.get(j).height *2;
                params1.width = (int) sizes.get(j).width;
                surfaceView.setLayoutParams(params1);
            }
        }

        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        try {
            if(!mInitSuccesful)
                initRecorder(surfaceHolder.getSurface());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    @Override
    public void onClickButtonSuccess() {

        if (!isRecord) {
            mCamera.stopPreview();
            mCamera.unlock();
            isRecord = true;
            mMediaRecorder.start();
        }
        else {
            isRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            try {
                initRecorder(surfaceHolder.getSurface());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if(mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setCamera(mCamera);

        File file = new File(Environment.getExternalStorageDirectory(),"video.mp4");
        try
        {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setVideoSize(640,480);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH));
    // Step 4: Set output file
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(surface);
        // Step 6: Prepare configured MediaRecorder
        mMediaRecorder.setMaxDuration(3600* 1000);
        mMediaRecorder.setMaxFileSize(-1);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
    }
}
