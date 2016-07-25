package com.sevenheaven.painter.actions;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;

/**
 * Created by 7heaven on 16/5/26.
 */
public class PictureAction extends Action {

    private Picture mPicture;
    private Matrix mMatrix;

    public PictureAction(Picture picture){
        mPicture = picture;
    }

    public void drawOnCanvas(Canvas canvas, Paint paint){
        if(mPicture != null){
            mPicture.draw(canvas);
        }
    }

    public void performMatrix(Matrix m){

    }
}
