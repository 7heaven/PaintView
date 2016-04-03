package com.sevenheaven.paintview.actions;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by 7heaven on 16/3/9.
 */
public class DrawPathAction extends Action {

    private Path mPath;
    private Paint mPaint;

    public DrawPathAction(Path path){
        this.mPath = path;
    }

    public DrawPathAction(Path path, Paint paint){
        this.mPath = path;
        this.mPaint = paint;
    }

    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint){
        if(this.mPath != null){
            canvas.drawPath(this.mPath, this.mPaint != null ? mPaint : paint);
        }
    }
}
