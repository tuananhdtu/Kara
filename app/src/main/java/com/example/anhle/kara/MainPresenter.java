package com.example.anhle.kara;

/**
 * Created by anhle on 4/25/16.
 */
public class MainPresenter {

    private MainView mainView;

    public MainPresenter(MainView mainView) {
        this.mainView = mainView;
    }


    public void showVideoRecord(){
        mainView.onSuccess();
    }
}
