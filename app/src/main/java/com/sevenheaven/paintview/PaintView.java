package com.sevenheaven.paintview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sevenheaven.paintview.actions.Action;
import com.sevenheaven.paintview.actions.DrawPathAction;
import com.sevenheaven.paintview.actions.PatternAction;

import java.util.ArrayList;

/**
 * Created by caifangmao on 15/8/17.
 */
public class PaintView extends View {

    public enum Rotate{
        CW, CCW;
    }

    private ArrayList<Action> mAllDrawActions;
    private Path mCurrentDrawingPath;
    private Path mGuidingPath;

    private PathMeasure mGuidingPathMeasure;

    private float mStartPressure;
    private float mEndPressure;
    private float[] mGuidingMeasurePos = new float[2];
    private float[] mGuidingMeasureTan = new float[2];
    private static final double halfPI = Math.PI / 2D;
    private static final float pressureScale = 6;
    private float lastDistanceOfPath = 0;

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
        mGuidingPath = new Path();

        mGuidingPathMeasure = new PathMeasure(mGuidingPath, false);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
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
                mStartPressure = event.getPressure();
                mGuidingPath.reset();
                mGuidingPath.moveTo(event.getX(), event.getY());

                mCurrentDrawingPath.reset();

                int r = (int) (Math.random() * 255);
                int g = (int) (Math.random() * 255);
                int b = (int) (Math.random() * 255);

                mPaint.setColor(0xFF000000 | r << 16 | g << 8 | b);
                break;
            case MotionEvent.ACTION_MOVE:
                mEndPressure = event.getPressure();
                mGuidingPath.lineTo(event.getX(), event.getY());
                mGuidingPathMeasure.setPath(mGuidingPath, false);

                mGuidingPathMeasure.getPosTan(lastDistanceOfPath, mGuidingMeasurePos, mGuidingMeasureTan);
                double angle = Math.atan2(mGuidingMeasureTan[1], mGuidingMeasureTan[0]);
                PointF center = new PointF(mGuidingMeasurePos[0], mGuidingMeasurePos[1]);
                PointF leftTopPoint = centerRadiusPoint(center, angle - halfPI, mStartPressure * pressureScale);
                PointF leftBottomPoint = centerRadiusPoint(center, angle + halfPI, mStartPressure * pressureScale);

                mGuidingPathMeasure.getPosTan(mGuidingPathMeasure.getLength(), mGuidingMeasurePos, mGuidingMeasureTan);
                angle = Math.atan2(mGuidingMeasureTan[1], mGuidingMeasureTan[0]);
                center = new PointF(mGuidingMeasurePos[0], mGuidingMeasurePos[1]);
                PointF rightTopPoint = centerRadiusPoint(center, angle - halfPI, mEndPressure * pressureScale);
                PointF rightBottomPoint = centerRadiusPoint(center, angle + halfPI, mEndPressure * pressureScale);

                mCurrentDrawingPath.moveTo(leftTopPoint.x, leftTopPoint.y);
                mCurrentDrawingPath.lineTo(leftBottomPoint.x, leftBottomPoint.y);
                mCurrentDrawingPath.lineTo(rightBottomPoint.x, rightBottomPoint.y);
                mCurrentDrawingPath.lineTo(rightTopPoint.x, rightTopPoint.y);
                mCurrentDrawingPath.close();

                mStartPressure = event.getPressure();
                lastDistanceOfPath = mGuidingPathMeasure.getLength();

                break;
            case MotionEvent.ACTION_UP:
                mDrawingCanvas.drawPath(mCurrentDrawingPath, mPaint);

                mAllDrawActions.add(new DrawPathAction(new Path(mCurrentDrawingPath), new Paint(mPaint)));

                mGuidingPath.reset();
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

        Log.w("rotate", ":" + mCurrentRotateAngle);

        matrix.setRotate(mCurrentRotateAngle, mDrawingCanvas.getWidth() / 2, mDrawingCanvas.getHeight() / 2);

//        for(Action action : mAllDrawActions){
//            action.setMatrix(new Matrix(matrix));
//        }

        mDrawingCanvas.save();
        mDrawingCanvas.setMatrix(matrix);

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

        mDrawingCanvas.restore();

        invalidate();
    }

    public void randomPattern(){
        if(randomPattern == null){
            randomPattern = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher);
        }

        int left = (int) (Math.random() * mDrawingBitmap.getWidth());
        int top = (int) (Math.random() * mDrawingBitmap.getHeight());

        mDrawingCanvas.drawBitmap(randomPattern, left, top, mPaint);

        mAllDrawActions.add(new PatternAction(randomPattern, new Rect(left, top, left + randomPattern.getWidth(), top + randomPattern.getHeight())));

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawBitmap(mDrawingBitmap, mCenterX - cachedBitmapHWidth, mCenterY - cachedBitmapHHeight, mPaint);
        canvas.drawPath(mCurrentDrawingPath, mPaint);
    }

    private PointF centerRadiusPoint(PointF center, double angle, float radius){
        float x = (float) (radius * Math.cos(angle) + center.x);
        float y = (float) (radius * Math.sin(angle) + center.y);

        return new PointF(x, y);
    }
}
