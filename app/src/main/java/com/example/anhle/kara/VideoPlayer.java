package com.example.anhle.kara;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;


/**
 * Created by anhle on 4/25/16.
 */
public class VideoPlayer extends Activity  {
    VideoView videoView;
    Button btnPlay;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        setContentView(R.layout.video_record);

        path = getIntent().getExtras().getString(Config.YUNINFO_RESULT_DATA);


        videoView = (VideoView) findViewById(R.id.videopreview);
        btnPlay =(Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setVideoPath(path);
                videoView.setMediaController(new MediaController(getApplicationContext()));
                videoView.requestFocus();
                videoView.start();
            }
        });
    }

}
