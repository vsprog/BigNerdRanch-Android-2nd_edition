package com.devel.draganddraw;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Box implements Parcelable {
    private PointF mOrigin;   // точка касания
    private PointF mCurrent;  // точка "отпускания"
    public float angle;

    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    protected Box(Parcel in) {
        mOrigin = in.readParcelable(PointF.class.getClassLoader());
        mCurrent = in.readParcelable(PointF.class.getClassLoader());
    }

    public static final Parcelable.Creator<Box> CREATOR = new Parcelable.Creator<Box>() {
        @Override
        public Box createFromParcel(Parcel in) {
            return new Box(in);
        }

        @Override
        public Box[] newArray(int size) {
            return new Box[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mOrigin);
        dest.writeValue(mCurrent);
    }

}
