package com.sevenheaven.painter.actions;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Represent a path drawing action
 *
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

    public void setPath(Path path){
        this.mPath = path;
    }

    public Path getPath(){
        return this.mPath;
    }

    public void setPaint(Paint paint){
        this.mPaint = paint;
    }

    public Paint getPaint(){
        return mPaint;
    }
}
