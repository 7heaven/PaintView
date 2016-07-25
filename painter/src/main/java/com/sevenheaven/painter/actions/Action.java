package com.sevenheaven.painter.actions;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 *
 * Represent a drawing action of PaintView
 *
 * Created by 7heaven on 16/3/9.
 */
abstract public class Action{

    private Matrix mMatrix;

    public Action(){}

    public void draw(Canvas canvas, Paint paint){
        if(mMatrix != null){
            canvas.save();
            Matrix oldMatrix = canvas.getMatrix();
            canvas.setMatrix(mMatrix);
            drawOnCanvas(canvas, paint);
            canvas.restore();

        }else{
            drawOnCanvas(canvas, paint);
        }
    }

    /**
     * do the drawing work
     * @param canvas
     * @param paint
     */
    protected abstract void drawOnCanvas(Canvas canvas, Paint paint);

    public void performMatrix(Matrix m){}
}