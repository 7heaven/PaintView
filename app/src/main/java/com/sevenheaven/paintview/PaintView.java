package com.sevenheaven.paintview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by caifangmao on 15/8/17.
 */
public class PaintView extends View {

    public enum Rotate{
        CW, CCW;
    }

    public enum DrawAction{
        DrawLine, Pattern
    }

    public static class Action{

        private DrawAction mDrawAction;

        private Path path;
        private int resName;
        private Bitmap bitmap;
        private int bitmapLeft;
        private int bitmapTop;

        private Matrix matrix;

        private Drawable drawable;

        public Action(DrawAction drawAction){
            mDrawAction = drawAction;
        }

        public Action(DrawAction drawAction, Bitmap bitmap, int left, int top){
            this(drawAction);
            this.bitmap = bitmap;

            this.bitmapLeft = left;
            this.bitmapTop = top;
        }

        public Action(DrawAction drawAction, int resName){
            this(drawAction);
            this.resName = resName;
        }

        public Action(DrawAction drawAction, Drawable drawable){
            this(drawAction);
            this.drawable = drawable;
        }

        public Action(DrawAction drawAction, Path path){
            this(drawAction);
            this.path = path;
        }

        public void setMatrix(Matrix matrix){
            if(this.matrix != null){
                this.matrix.postConcat(matrix);
            }else{
                this.matrix = matrix;
            }
        }

        public void drawOnCanvas(Canvas canvas, Paint paint){
            int savedCount = 0;
            if(matrix != null){
                savedCount = canvas.save();
                canvas.setMatrix(matrix);
            }

            switch(mDrawAction){
                case DrawLine:
                    if(path != null){
                        canvas.drawPath(path, paint);
                    }
                    break;
                case Pattern:
                    if(resName != 0){
                    }else if(drawable != null){
                        drawable.draw(canvas);
                    }else if(bitmap != null){
                        canvas.drawBitmap(bitmap, bitmapLeft, bitmapTop, paint);
                    }
                    break;
            }

            if(savedCount > 0) canvas.restoreToCount(savedCount);
        }
    }

    private ArrayList<Action> mAllDrawActions;
    private Path mCurrentDrawingPath;

    private Bitmap randomPattern;

    private Canvas mDrawingCanvas;
    private Bitmap mDrawingBitmap;

    private Rect drawingRect;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;

    private int cachedBitmapHWidth;
    private int cachedBitmapHHeight;

    private int paintColor = 0xFF0099CC;
    private int backgroundColor = 0xFFFFFFFF;

    private int mCurrentRotateAngle = 0;

    public PaintView(Context context){
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        mAllDrawActions = new ArrayList<Action>();
        mCurrentDrawingPath = new Path();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(0xFF0099CC);

        drawingRect = new Rect();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        mDrawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawingCanvas = new Canvas(mDrawingBitmap);

        cachedBitmapHWidth = mDrawingBitmap.getWidth()  / 2;
        cachedBitmapHHeight = mDrawingBitmap.getHeight() / 2;

        mWidth = w;
        mHeight = h;
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mCurrentDrawingPath.reset();
                mCurrentDrawingPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentDrawingPath.lineTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mDrawingCanvas.drawPath(mCurrentDrawingPath, mPaint);

                mAllDrawActions.add(new Action(DrawAction.DrawLine, new Path(mCurrentDrawingPath)));

                mCurrentDrawingPath.reset();
                break;
        }

        invalidate();

        return true;
    }



    public void undo(){
        if(mAllDrawActions != null && mAllDrawActions.size() > 0){
            mAllDrawActions.remove(mAllDrawActions.size() - 1);

            drawingRect.left = 0;
            drawingRect.top = 0;
            drawingRect.bottom = mDrawingBitmap.getHeight();
            drawingRect.right = mDrawingBitmap.getWidth();

            mPaint.setColor(backgroundColor);
            mPaint.setStyle(Paint.Style.FILL);
            mDrawingCanvas.drawRect(drawingRect, mPaint);

            mPaint.setColor(paintColor);
            mPaint.setStyle(Paint.Style.STROKE);

            for(int i = 0; i < mAllDrawActions.size(); i++){
                mAllDrawActions.get(i).drawOnCanvas(mDrawingCanvas, mPaint);
            }

            invalidate();
        }
    }

    public void rotate(Rotate rotate){
        Matrix matrix = new Matrix();

        switch(rotate){
            case CW:
                mCurrentRotateAngle += 90;
                break;
            case CCW:
                mCurrentRotateAngle -= 90;
                break;
        }

        matrix.postRotate(mCurrentRotateAngle, mDrawingCanvas.getWidth() / 2, mDrawingCanvas.getHeight() / 2);

        for(Action action : mAllDrawActions){
            action.setMatrix(new Matrix(matrix));
        }

        drawingRect.left = 0;
        drawingRect.top = 0;
        drawingRect.bottom = mDrawingBitmap.getHeight();
        drawingRect.right = mDrawingBitmap.getWidth();

        mPaint.setColor(backgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        mDrawingCanvas.drawRect(drawingRect, mPaint);

        mPaint.setColor(paintColor);
        mPaint.setStyle(Paint.Style.STROKE);

        for(int i = 0; i < mAllDrawActions.size(); i++){
            mAllDrawActions.get(i).drawOnCanvas(mDrawingCanvas, mPaint);
        }

        invalidate();
    }

    public void randomPattern(){
        if(randomPattern == null){
            randomPattern = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher);
        }

        int left = (int) (Math.random() * mDrawingBitmap.getWidth());
        int top = (int) (Math.random() * mDrawingBitmap.getHeight());

        mDrawingCanvas.drawBitmap(randomPattern, left, top, mPaint);

        mAllDrawActions.add(new Action(DrawAction.Pattern, randomPattern, left, top));
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawBitmap(mDrawingBitmap, mCenterX - cachedBitmapHWidth, mCenterY - cachedBitmapHHeight, mPaint);
        canvas.drawPath(mCurrentDrawingPath, mPaint);
    }
}
