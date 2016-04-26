package com.example.anhle.kara;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by anhlee on 4/26/16.
 */
public class ProgressView extends View {

    Paint paint;
    Paint paintCircle;
    float width;
    float position;
    float duration;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        //set màu và độ rộng đường line
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);

        paintCircle = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //vẽ đường line
        canvas.drawLine(20, 20, width, 20, paint);

        //Vẽ hình tròn
        paintCircle.setColor(Color.MAGENTA);
        float xPosition=position;
        float yPosition=20;
        int size = 10;
        canvas.drawCircle(xPosition, yPosition, size, paintCircle);
    }

    public void setWidth( float width){
        this.width = width; //độ dài đường line
    }

    public void setDuration( float duration){
        this.duration = duration; //độ dài đường line
    }

    public void setPosition( float  position){
        position = width * position/duration + 20;
        this.position = position; //vị trí đang play
    }
}
