package com.sevenheaven.paintview.actions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

/**
 * Created by 7heaven on 16/3/9.
 */
abstract public class Action{

    public enum DrawAction{
        DrawPath, Pattern
    }

    private DrawAction mDrawAction;

    private Path path;
    private int resName;
    private Bitmap bitmap;
    private int bitmapLeft;
    private int bitmapTop;

    private Matrix matrix;

    private Drawable drawable;

    public Action(){}

//    public Action(DrawAction drawAction, Bitmap bitmap, int left, int top){
//        this(drawAction);
//        this.bitmap = bitmap;
//
//        this.bitmapLeft = left;
//        this.bitmapTop = top;
//    }
//
//    public Action(DrawAction drawAction, int resName){
//        this(drawAction);
//        this.resName = resName;
//    }
//
//    public Action(DrawAction drawAction, Drawable drawable){
//        this(drawAction);
//        this.drawable = drawable;
//    }
//
//    public Action(DrawAction drawAction, Path path){
//        this(drawAction);
//        this.path = path;
//    }
//
//    public void setMatrix(Matrix matrix){
//        if(this.matrix != null){
//            this.matrix.postConcat(matrix);
//        }else{
//            this.matrix = matrix;
//        }
//    }

    abstract public void drawOnCanvas(Canvas canvas, Paint paint);
//        int savedCount = 0;
//        if(matrix != null){
//            savedCount = canvas.save();
//            canvas.setMatrix(matrix);
//        }
//
//        switch(mDrawAction){
//            case DrawPath:
//                if(path != null){
//                    canvas.drawPath(path, paint);
//                }
//                break;
//            case Pattern:
//                if(resName != 0){
//                }else if(drawable != null){
//                    drawable.draw(canvas);
//                }else if(bitmap != null){
//                    canvas.drawBitmap(bitmap, bitmapLeft, bitmapTop, paint);
//                }
//                break;
//        }
//
//        if(savedCount > 0) canvas.restoreToCount(savedCount);
//    }
}