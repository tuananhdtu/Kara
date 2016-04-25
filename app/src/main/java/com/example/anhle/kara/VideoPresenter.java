package com.example.anhle.kara;

/**
 * Created by anhle on 4/25/16.
 */
public class VideoPresenter {
    private VideoView videoView;

    public VideoPresenter(VideoView videoView) {
        this.videoView = videoView;
    }


    public void clickButtonRecord(){
        videoView.onClickButtonSuccess();
    }
}

