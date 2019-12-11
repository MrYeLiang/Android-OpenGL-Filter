package com.yeliang;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.yeliang.widget.CommonSurfaceView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private CommonSurfaceView mSurfaceView;

    private static String TAG = "MainActivity";

    private boolean mCameraIsOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏，隐藏状态
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surfaceview);
        findViewById(R.id.btn_open_camera).setOnClickListener(this);
        findViewById(R.id.btn_close_camera).setOnClickListener(this);
        findViewById(R.id.btn_record).setOnTouchListener(this);

        requestPermission();
    }

    private void requestPermission() {
        int CameraPermissionResult = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (CameraPermissionResult == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1000);
        }

        int storagePermissionResult = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storagePermissionResult == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2000);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_camera:
                mCameraIsOpen = true;
                mSurfaceView.openCamera();
                break;
            case R.id.btn_close_camera:
                mCameraIsOpen = false;
                mSurfaceView.closeCamera();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.closeCamera();
        mCameraIsOpen = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mCameraIsOpen) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "onTouch ACTION_DOWN");
                mSurfaceView.startRecord();

                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "onTouch ACTION_UP");
            case MotionEvent.ACTION_CANCEL:
                mSurfaceView.stopRecord();
                break;
        }

        return false;
    }
}
