package com.deston.clipimageview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.deston.cache.BitmapDiskCache;

public class ClipSampleActivity extends Activity {
    public final static int FOR_RESULT_GET_IMAGE = 1;
    public final static int FOR_RESULT_EDIT_PHOTO = 2;
    private ImageView mUserPhotoIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_layout);
        initViews();
        setListeners();
    }

    private void initViews() {
        mUserPhotoIv = (ImageView) findViewById(R.id.user_photo_iv);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.user_photo_iv:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, FOR_RESULT_GET_IMAGE);
                    break;
            }
        }
    };

    private void setListeners() {
        mUserPhotoIv.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FOR_RESULT_GET_IMAGE:
                if (data != null) {
                    Uri uri = data.getData();
                    Intent intent = new Intent(ClipSampleActivity.this, PhotoEditActivity.class);
                    intent.setData(uri);
                    startActivityForResult(intent, FOR_RESULT_EDIT_PHOTO);
                }
                break;
            case FOR_RESULT_EDIT_PHOTO:
                if (data != null) {
                    String filePath = data.getStringExtra(PhotoEditActivity.CACHE_KEY_BITMAP_RESULT);
                    Bitmap bitmap = BitmapDiskCache.getInstance(ClipSampleActivity.this.getCacheDir().getPath()).get(filePath, mUserPhotoIv.getWidth(), mUserPhotoIv.getHeight(), 30);
                    mUserPhotoIv.setImageBitmap(bitmap);
                }
                break;
        }
    }
}
