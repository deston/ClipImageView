package com.deston.clipimageview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.deston.BitmapDecoder;
import com.deston.cache.BitmapDiskCache;
import com.deston.util.DeviceUtil;
import com.deston.view.ClipImageView;

public class PhotoEditActivity extends Activity {
    private Button mCommitBtn;
    private ClipImageView mClipIv;
    public final static String CACHE_KEY_BITMAP_RESULT = "KEY_BITMAP_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_edit_layout);
        initViews();
    }

    private void initViews() {
        mClipIv = (ClipImageView) findViewById(R.id.photo_edit_iv);
        mCommitBtn = (Button) findViewById(R.id.photo_edit_commit_btn);
        final Intent intent = getIntent();
        if (intent.getData() != null) {
            mClipIv.setImageBitmap(BitmapDecoder.decodeUri(this, intent.getData(), DeviceUtil.getDeviceWidth(this), DeviceUtil.getDeviceHeight(this)));
        }
        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                String filePath = BitmapDiskCache.getInstance(PhotoEditActivity.this.getCacheDir().getPath()).createCacheFilePath();
                intent1.putExtra(CACHE_KEY_BITMAP_RESULT, filePath);
                BitmapDiskCache.getInstance(PhotoEditActivity.this.getCacheDir().getPath()).put(filePath, mClipIv.getClipBitmap());
                setResult(ClipSampleActivity.FOR_RESULT_EDIT_PHOTO, intent1);
                finish();
            }
        });

    }


}
