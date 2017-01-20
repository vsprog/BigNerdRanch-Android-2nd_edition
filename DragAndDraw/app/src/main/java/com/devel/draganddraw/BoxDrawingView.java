package com.devel.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BoxDrawingView extends View {

    private static final String TAG = "BoxDrawingView";

    private Box mCurrentBox;
    private ArrayList<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    public BoxDrawingView(Context context) {
        this(context, null);
    }

    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.rotate(box.angle);
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.rotate(-box.angle);
        }
    }

    //@Override
    public boolean onTouchEvent11(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN";

                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";

                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(current);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;

                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "ACTION_POINTER_UP";

                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;

                break;
        }

        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);

        return true;
    }

    @Override
    public boolean onTouchEvent (MotionEvent e) {
        PointF sPoint = new PointF();
        int action = e.getActionMasked();
        int pointerCount = e.getPointerCount();
        for(int i = 0; i < pointerCount; i++){
            PointF current = new PointF(e.getX(i), e.getY(i));
            int pointerId = e.getPointerId(i);
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    mCurrentBox = new Box(current);
                    mBoxen.add(mCurrentBox);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(pointerId == 0){
                        if(mCurrentBox == null){
                            mCurrentBox = new Box(current);
                            mBoxen.add(mCurrentBox);
                        }
                        break;
                    }else if(pointerId == 1 && mCurrentBox!=null){
                        sPoint = current;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(pointerId == 0){
                        mCurrentBox = null;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    if(pointerId == 0){
                        mCurrentBox = null;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mCurrentBox != null){
                        if(pointerId == 0){
                            mCurrentBox.setCurrent(current);
                            invalidate();
                        }else if(pointerId == 1){
                            mCurrentBox.angle = angleBetweenLines(mCurrentBox.getCurrent().x,
                                    mCurrentBox.getCurrent().y,
                                    sPoint.x, sPoint.y,
                                    mCurrentBox.getCurrent().x,
                                    mCurrentBox.getCurrent().y,
                                    current.x, current.y);
                            Log.d(TAG, "Angle = " + mCurrentBox.angle);
                            invalidate();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if(pointerId == 0){
                        mCurrentBox = null;
                    }
                    break;
            }
        } return true;
    }

   /* public static float angleBetweenLines(float A1x, float A1y, float A2x, float A2y, float B1x, float B1y, float B2x, float B2y) {
        float angle1 = (float) Math.atan2(A2y - A1y, A1x - A2x);
        float angle2 = (float) Math.atan2(B2y - B1y, B1x - B2x);
        float calculatedAngle = (float) Math.toDegrees(angle1 - angle2);
        if (calculatedAngle < 0) calculatedAngle += 360;
        return calculatedAngle;
    }*/

    private float angleBetweenLines(float fx, float fy, float sx, float sy, float nfx, float nfy, float nsx, float nsy) {
        float angle1 = (float) Math.atan2(sy - fy, sx - fx);
        float angle2 = (float) Math.atan2(nsy - nfy, nsx - nfx);

        float angle = (float) (Math.toDegrees(angle2 - angle1) % 360);
        if (angle < 0f) { angle+=360f; }
        return angle;
    }

    //----------------------bundle-------------------------------------
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        //bundle.putParcelable("instanceState", super.onSaveInstanceState());
        //bundle.putSerializable("test", mBoxen);
        bundle.putParcelableArrayList("instanceState", (ArrayList<? extends Parcelable>) mBoxen);
        bundle.putParcelable("test",super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
       /* if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mBoxen = (ArrayList<Box>) bundle.getSerializable("test");
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }
        super.onRestoreInstanceState(state);*/
        Bundle bundle = (Bundle) state;
        Parcelable superViewState = bundle.getParcelable("test");
        mBoxen = bundle.getParcelableArrayList("instanceState");
        super.onRestoreInstanceState(superViewState);

    }
    //-----------------------Parcelable-----------------------------------
   /* @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mBoxList = mBoxen;
        return savedState;
    }
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState mSavedState = (SavedState) state;
        super.onRestoreInstanceState(mSavedState.getSuperState());
        mBoxen = mSavedState.mBoxList;
    }

    private static class SavedState extends BaseSavedState {
        private List<Box> mBoxList;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mBoxList = (ArrayList<Box>) in.readValue(getClass().getClassLoader());
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mBoxList);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }*/
}
