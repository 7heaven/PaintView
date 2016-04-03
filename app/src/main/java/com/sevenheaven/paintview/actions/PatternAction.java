package com.sevenheaven.paintview.actions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by 7heaven on 16/3/9.
 */
public class PatternAction extends Action {

    private Bitmap mBitmap;
    private Rect mBound;

    public PatternAction(Bitmap bitmap, Rect bound){
        this.mBitmap = bitmap;
        this.mBound = bound;
    }

    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint){
        if(this.mBitmap != null && this.mBound != null){
            canvas.drawBitmap(this.mBitmap, null, this.mBound, paint);
        }
    }
}
