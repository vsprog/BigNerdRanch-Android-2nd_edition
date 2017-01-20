package com.devel.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, Activity activity){
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap getScaledBitmap (String path, int destWidth, int destHeight){
        //чтение размеров изображения на диске
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        //вычисление степени масштабирования
        int inSampleSize = 1;
        if(srcHeight > destHeight || srcWidth > destWidth){
            if (srcWidth > srcHeight)
                inSampleSize = Math.round(srcHeight / destHeight);
            else
                inSampleSize = Math.round(srcWidth / destWidth);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        //чтение данных и сохдание нового изображения
        return BitmapFactory.decodeFile(path, options);
    }
}
