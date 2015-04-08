package com.deston.cache;

import android.graphics.Bitmap;
import com.deston.BitmapDecoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BitmapDiskCache implements Cache<Bitmap> {
    public static int MAX_IMAGE_CACHE_SIZE = 10 * 1024 * 1024;
    private String mCacheDir;
    private long mMaxCacheSize;
    private long mCurrentTotalZize;
    private static Map<String, BitmapDiskCache> mInstanceMap = new HashMap<String, BitmapDiskCache>();
    public synchronized static BitmapDiskCache getInstance(String cacheDir) {
        if (mInstanceMap.get(cacheDir) == null) {
            BitmapDiskCache bitmapDiskCache = new BitmapDiskCache(cacheDir);
            bitmapDiskCache.initialize();
            mInstanceMap.put(cacheDir, bitmapDiskCache);
            return bitmapDiskCache;
        }
        return mInstanceMap.get(cacheDir);
    }

    private void initialize() {
        this.mMaxCacheSize = MAX_IMAGE_CACHE_SIZE;
        File dir = new File(mCacheDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        mCurrentTotalZize = 0;
        for (File file : files) {
            mCurrentTotalZize += file.length();
        }
    }

    private BitmapDiskCache(String cacheDir) {
        mCacheDir = cacheDir;
    }


    /**
     * 根据时间戳创建文件名
     *
     * @return
     */
    public String createCacheFilePath() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        return mCacheDir + "/" + df.format(new Date()) + ".png";
    }

    @Override
    public Bitmap get(String filePath) {
        return get(filePath, 0, 0, 100);
    }

    public Bitmap get(String filePath, int requestWidth, int requestHeight, int quality) {
        return BitmapDecoder.decodeFile(filePath, requestWidth, requestHeight, quality);
    }





    @Override
    public void put(String filePath, Bitmap value) {
        long needSpace = BitmapDecoder.getBitmapSize(value);
        prunesIfNeed(needSpace);
        mCurrentTotalZize += needSpace;
        File file = new File(filePath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            value.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        File cacheDir = new File(mCacheDir);
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        mCurrentTotalZize = 0;
    }

    private void prunesIfNeed(long needSpace) {
        if (mCurrentTotalZize + needSpace > mMaxCacheSize) {
            File cacheDir = new File(mCacheDir);
            if (cacheDir.isDirectory()) {
                File[] files = cacheDir.listFiles();
                for (File file : files) {
                    long fileSize = file.length();
                    if (file.delete()) {
                        mCurrentTotalZize -= fileSize;
                        if (mCurrentTotalZize <= mMaxCacheSize) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
