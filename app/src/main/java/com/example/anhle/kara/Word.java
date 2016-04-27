package com.example.anhle.kara;

/**
 * Created by anhle on 4/27/16.
 */
public class Word {
    private static final String TAG = Word.class.getSimpleName();

    public long startTime;
    public String stringTime;

    public long getStartTime() {
        return startTime*1000;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getStringTime() {
        return stringTime;
    }

    public void setStringTime(String stringTime) {
        this.stringTime = stringTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String text;
}
