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
import android.support.annotation.DrawableRes;
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
    private Path mCurrentMovePath;

    private PathMeasure mGuidingPathMeasure;

    private float mStartPressure;
    private float mEndPressure;
    private float[] mGuidingMeasurePos = new float[2];
    private float[] mGuidingMeasureTan = new float[2];
    private static final double halfPI = Math.PI / 2D;
    private static final float pressureScale = 50.0F;
    private float lastDistanceOfPath = 0;
    private boolean isFirstDown = false;

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
        mCurrentMovePath = new Path();

        mGuidingPathMeasure = new PathMeasure(mGuidingPath, false);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(paintColor);

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
        Log.w("pressure", ":" + event.getSize());
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mStartPressure = event.getSize();
                mGuidingPath.reset();
                mGuidingPath.moveTo(event.getX(), event.getY());

                mCurrentDrawingPath.reset();
                mCurrentMovePath.reset();
                isFirstDown = true;

                int r = (int) (Math.random() * 255);
                int g = (int) (Math.random() * 255);
                int b = (int) (Math.random() * 255);

                mPaint.setColor(0xFF000000 | r << 16 | g << 8 | b);
                mPaint.setStyle(Paint.Style.STROKE);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mEndPressure = event.getSize();
                mGuidingPath.lineTo(event.getX(), event.getY());
                mGuidingPathMeasure.setPath(mGuidingPath, false);

                float startPressureScale = mStartPressure * pressureScale;
                float endPressureScale = mEndPressure * pressureScale;

                mGuidingPathMeasure.getPosTan(lastDistanceOfPath, mGuidingMeasurePos, mGuidingMeasureTan);
                double startAngle = Math.atan2(mGuidingMeasureTan[1], mGuidingMeasureTan[0]);
                PointF startCenter = new PointF(mGuidingMeasurePos[0], mGuidingMeasurePos[1]);
                PointF leftTopPoint = centerRadiusPoint(startCenter, startAngle - halfPI, startPressureScale);
                PointF leftBottomPoint = centerRadiusPoint(startCenter, startAngle + halfPI, startPressureScale);

                mGuidingPathMeasure.getPosTan(mGuidingPathMeasure.getLength(), mGuidingMeasurePos, mGuidingMeasureTan);
                double endAngle = Math.atan2(mGuidingMeasureTan[1], mGuidingMeasureTan[0]);
                PointF endCenter = new PointF(mGuidingMeasurePos[0], mGuidingMeasurePos[1]);
                PointF rightTopPoint = centerRadiusPoint(endCenter, endAngle - halfPI, endPressureScale);
                PointF rightBottomPoint = centerRadiusPoint(endCenter, endAngle + halfPI, endPressureScale);

                PointF curveStartPoint = centerRadiusPoint(startCenter, startAngle + Math.PI, startPressureScale);
                PointF curveEndPoint = centerRadiusPoint(endCenter, endAngle, endPressureScale);

                if(isFirstDown){
                    mCurrentMovePath.reset();
//                    mCurrentMovePath.moveTo(leftTopPoint.x, leftTopPoint.y);
//                    mCurrentMovePath.quadTo(curveStartPoint.x, curveStartPoint.y, leftBottomPoint.x, leftBottomPoint.y);
//                    mCurrentMovePath.lineTo(rightBottomPoint.x, rightBottomPoint.y);
//                    mCurrentMovePath.quadTo(curveEndPoint.x, curveEndPoint.y, rightTopPoint.x, rightTopPoint.y);
//                    mCurrentMovePath.close();
                    mCurrentMovePath.moveTo(rightBottomPoint.x, rightBottomPoint.y);
                    mCurrentMovePath.lineTo(leftBottomPoint.x, leftBottomPoint.y);
                    mCurrentMovePath.quadTo(curveStartPoint.x, curveStartPoint.y, leftTopPoint.x, leftTopPoint.y);
                    mCurrentMovePath.lineTo(rightTopPoint.x, rightTopPoint.y);
                    isFirstDown = false;
                }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                    mCurrentMovePath.reset();
                    mCurrentMovePath.moveTo(leftTopPoint.x, leftTopPoint.y);
                    mCurrentMovePath.lineTo(rightTopPoint.x, rightTopPoint.y);
                    mCurrentMovePath.moveTo(leftBottomPoint.x, leftBottomPoint.y);
                    mCurrentMovePath.lineTo(rightBottomPoint.x, rightBottomPoint.y);
                }else{
                    mCurrentMovePath.reset();
                    mCurrentMovePath.moveTo(leftTopPoint.x, leftTopPoint.y);
                    mCurrentMovePath.lineTo(rightTopPoint.x, rightTopPoint.y);
                    mCurrentMovePath.quadTo(curveEndPoint.x, curveEndPoint.y, rightBottomPoint.x, rightBottomPoint.y);
                    mCurrentMovePath.lineTo(leftBottomPoint.x, leftBottomPoint.y);
                }

//                PointF startNormalPoint = centerRadiusPoint(startCenter, startAngle, 50);
//                PointF endNormalPoint = centerRadiusPoint(endCenter, endAngle + Math.PI, 100);
//
////                PointF middleSegmentPoint = centerRadiusPoint(startCenter, startAngle, (mGuidingPathMeasure.getLength() - lastDistanceOfPath) / 2);
//                PointF middleSegmentPoint = getCross(startCenter, startNormalPoint, endCenter, endNormalPoint);
//                if(middleSegmentPoint == null){
//                    middleSegmentPoint = new PointF((startCenter.x + endCenter.x) / 2, (startCenter.y + endCenter.y) / 2);
//                }
//                float middlePressure = (mStartPressure + mEndPressure) / 2F;
//                double middleAngle = (startAngle + endAngle) / 2D;
//                PointF upperCtlPoint = centerRadiusPoint(middleSegmentPoint, middleAngle - halfPI, middlePressure * pressureScale);
//                PointF lowerCtlPoint = centerRadiusPoint(middleSegmentPoint, middleAngle + halfPI, middlePressure * pressureScale);
//
//                mCurrentMovePath.reset();
//
////                mCurrentMovePath.addCircle(startNormalPoint.x, startNormalPoint.y, 10, Path.Direction.CCW);
////                mCurrentMovePath.addCircle(endNormalPoint.x, endNormalPoint.y, 10, Path.Direction.CCW);
////                mCurrentMovePath.addCircle(middleSegmentPoint.x, middleSegmentPoint.y, 10, Path.Direction.CCW);
//                mCurrentMovePath.moveTo(startCenter.x, startCenter.y);
//                mCurrentMovePath.lineTo(startNormalPoint.x, startNormalPoint.y);
//                mCUrrentMovePath.moveTo(endCenterx)
//
//                mCurrentMovePath.moveTo(leftTopPoint.x, leftTopPoint.y);
//                mCurrentMovePath.lineTo(rightBottomPoint.x, rightBottomPoint.y);
//                mCurrentMovePath.close();

                mCurrentDrawingPath.addPath(mCurrentMovePath);
                mDrawingCanvas.drawPath(mCurrentMovePath, mPaint);

                invalidate();

                mStartPressure = mEndPressure;
                lastDistanceOfPath = mGuidingPathMeasure.getLength();

                if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                    mAllDrawActions.add(new DrawPathAction(new Path(mCurrentDrawingPath), new Paint(mPaint)));

                    mGuidingPath.reset();
                    mCurrentMovePath.reset();
                    mCurrentDrawingPath.reset();

                    invalidate();
                }
        }



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

        for(int i = 0; i < mAllDrawActions.size(); i++){
            mAllDrawActions.get(i).drawOnCanvas(mDrawingCanvas, mPaint);
        }

        mDrawingCanvas.restore();

        invalidate();
    }

    public void randomPattern(int resourceID){
        randomPattern(BitmapFactory.decodeResource(getContext().getResources(), resourceID));
    }

    public void randomPattern(Bitmap pattern){
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
        canvas.drawPath(mCurrentMovePath, mPaint);
    }

    private PointF centerRadiusPoint(PointF center, double angle, double radius){
        float x = (float) (radius * Math.cos(angle) + center.x);
        float y = (float) (radius * Math.sin(angle) + center.y);

        return new PointF(x, y);
    }

    private PointF getCross(PointF l1P1, PointF l1P2, PointF l2P1, PointF l2P2){
        PointF resultP = new PointF(0, 0);

        double num = (l1P2.y - l1P1.y) * (l1P1.x - l2P1.x) - (l1P2.x - l1P1.x) * (l1P1.y - l2P1.y);
        double denom = (l1P2.y - l1P1.y) * (l2P2.x - l2P1.x) - (l1P2.x - l1P1.x) * (l2P2.y - l2P1.y);

        if(denom == 0) return null;

        resultP.x = (float) (l2P1.x + (l2P2.x - l2P1.x) * num / denom);
        resultP.y = (float) (l2P1.y + (l2P2.y - l2P1.y) * num / denom);

        return resultP;
    }
}
