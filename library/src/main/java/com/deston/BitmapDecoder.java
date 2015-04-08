package com.deston;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import java.io.*;

public class BitmapDecoder {

    public static Bitmap decodeUri(Context context, Uri uri, int requestWidth, int requestHeight, int quality) {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream in;
        try {
            in = contentResolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int sampleSize = calcuteSampleSize(options, requestWidth, requestHeight);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            in = contentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            if (quality < 100) {
                bitmap = compress(bitmap, quality);
            }
            in.close();
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeUri(Context context, Uri uri, int requestWidth, int requestHeight) {
        return decodeUri(context, uri, requestWidth, requestHeight, 100);
    }

    public static Bitmap decodeFile(String filePath, int requestWidth, int requestHeight) {
        return decodeFile(filePath, requestWidth, requestHeight, 100);
    }

    public static Bitmap decodeFile(String filePath, int requestWidth, int requestHeight, int quality) {
        Bitmap bitmap = null;
        try {
            InputStream in;
            in = new FileInputStream(new File(filePath));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            in = new FileInputStream(new File(filePath));
            options.inSampleSize = calcuteSampleSize(options, requestWidth, requestHeight);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, options);
            if (quality < 100) {
                bitmap = compress(bitmap, quality);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static int calcuteSampleSize(BitmapFactory.Options options, int requestWidth, int requestHeight) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int sampleSize = 1;
        if (outWidth > requestWidth || outHeight > requestHeight) {
            final int heightRatio = Math.round((float) outHeight / requestHeight);
            final int widthRatio = Math.round((float) outWidth / requestWidth);
            sampleSize = Math.max(heightRatio, widthRatio);
        }
        return sampleSize;
    }

    public static Bitmap compress(Bitmap source, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        source.compress(Bitmap.CompressFormat.PNG, quality, baos);
        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(in, null, null);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static long getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getRowBytes() * bitmap.getHeight();
        } else {
            return bitmap.getByteCount();
        }
    }

}
