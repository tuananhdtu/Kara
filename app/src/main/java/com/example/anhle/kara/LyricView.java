package com.example.anhle.kara;

/**
 * Created by anhle on 4/27/16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.example.anhle.kara.LyricUtils;
import com.example.anhle.kara.Lyric;

/**
 * A Scrollable TextView which use lyric stream as input and display it.
 * <p/>
 * Created by yifan on 5/13/14.
 */
public class LyricView extends TextView implements Runnable {
    public ArrayList<Lyric> lyric;

    private static final int DY = 50;
    private int mLyricSentenceLength;
    private Paint mCurrentPaint;
    private Paint mPaint;
    private float mMiddleX;
    private float mMiddleY;
    private int mHeight;
    long ts;
    private int mLyricIndex = 0;
    private boolean mIsNeedUpdate = false;

    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);

        int backgroundColor = Color.TRANSPARENT;
        int highlightColor = Color.RED;
        int normalColor = Color.WHITE;
        setBackgroundColor(backgroundColor);

        // Non-highlight part
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(36);
        mPaint.setColor(normalColor);
        mPaint.setTypeface(Typeface.SERIF);

        // highlight part, current lyric
        mCurrentPaint = new Paint();
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setColor(highlightColor);
        mCurrentPaint.setTextSize(36);
        mCurrentPaint.setTypeface(Typeface.SANS_SERIF);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mCurrentPaint.setTextAlign(Paint.Align.CENTER);
        setHorizontallyScrolling(true);
        setMovementMethod(new ScrollingMovementMethod());
    }

    private int drawText(Canvas canvas, Paint paint, String text, float startY) {
        int line = 0;
        ++line;
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, mMiddleX, startY, paint);
        return line;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (lyric == null)
            return;
        final List<Lyric> sentenceList = lyric;
        if (sentenceList == null || sentenceList.isEmpty() || mLyricIndex == -2) {
            return;
        }

        float currY;

        if (mLyricIndex > -1) {
            currY = mMiddleY + DY * drawText(
                    canvas, mPaint, sentenceList.get(mLyricIndex).showWord(), mMiddleY);
        } else {
            currY = mMiddleY + DY;
        }

        int size = sentenceList.size();
        for (int i = mLyricIndex + 1; i < size; i++) {
            if (currY > mHeight) {
                break;
            }
            currY += DY * drawText(canvas, mPaint, sentenceList.get(i).showWord(), currY);
        }

        currY = mMiddleY - DY;

        for (int i = mLyricIndex - 1; i >= 0; i--) {
            if (currY < 0) {
                break;
            }
            currY -= DY * drawText(canvas, mPaint, sentenceList.get(i).showWord(), currY);
        }
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        mMiddleX = w * 0.5f; // remember the center of the screen
        mHeight = h;
        mMiddleY = h * 0.5f;
    }

    public synchronized void setLyric(ArrayList<Lyric> lyric, boolean resetIndex) {
        this.lyric = lyric;
        mLyricSentenceLength = this.lyric.size();
        if (resetIndex) {
            mLyricIndex = 0;
        }
    }
    public boolean checkUpdate() {
        if (mIsNeedUpdate) {
            mIsNeedUpdate = false;
            return true;
        }
        return false;
    }
    public void setLyricIndex(int index) {
        mLyricIndex = index;
    }
    public long updateIndex(long time) {
        // Current index is last sentence
        if (mLyricIndex >= mLyricSentenceLength - 1) {
            mLyricIndex = mLyricSentenceLength - 1;
            return -1;
        }

        // Get index of sentence whose timestamp is between its startTime and currentTime.
        mLyricIndex = LyricUtils.getSentenceIndex(lyric, time, mLyricIndex, 0);

        // New current index is last sentence
        if (mLyricIndex >= mLyricSentenceLength - 1) {
            mLyricIndex = mLyricSentenceLength - 1;
            return -1;
        }

        return lyric.get(mLyricIndex + 1).words.get(0).getStartTime();
    }
    public synchronized void setLyric(ArrayList<Lyric> lyric) {
        setLyric(lyric, true);
    }

    public void play() {
        mStop = false;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        mStop = true;
    }

    private long mStartTime = -1;
    private boolean mStop = true;
    private boolean mIsForeground = true;
    private long mNextSentenceTime = -1;

    private Handler mHandler = new Handler();

    @Override
    public void run() {
        if (mStartTime == -1) {
            mStartTime = System.currentTimeMillis();
        }

        while (mLyricIndex != -2) {
            if (mStop) {
                return;
            }
            long ts = System.currentTimeMillis() - mStartTime;
            if (ts >= mNextSentenceTime || checkUpdate()) {
                mNextSentenceTime = updateIndex(ts);
                // Redraw only when window is visible
                if (mIsForeground) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                }
            }
            if (mNextSentenceTime == -1) {
                mStop = true;
            }
        }
    }

}