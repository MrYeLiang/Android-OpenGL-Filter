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

import com.yeliang.widget.CommonRender;
import com.yeliang.widget.CommonSurfaceView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private CommonSurfaceView mSurfaceView;

    private static String TAG = "MainActivity";

    private boolean mCameraIsOpen;
    private boolean mBeautyIsOpen;

    private Button btnRecord;
    private Button btnOpenCamera;
    private Button btnBeauty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏，隐藏状态
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surfaceview);

        btnOpenCamera = findViewById(R.id.btn_open_camera);
        btnOpenCamera.setOnClickListener(this);

        findViewById(R.id.btn_switch_camera).setOnClickListener(this);

        btnBeauty = findViewById(R.id.btn_open_beauty);
        btnBeauty.setOnClickListener(this);

        btnRecord = findViewById(R.id.btn_record);
        btnRecord.setOnTouchListener(this);

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
                openOrCloseCamera();
                break;
            case R.id.btn_open_beauty:
                openOrCloseBeauty();
                break;
            case R.id.btn_switch_camera:
                getCommonRender().switchCamera();
                break;
        }
    }

    private void openOrCloseBeauty() {
        if (mBeautyIsOpen) {
            mBeautyIsOpen = false;
            btnBeauty.setText("开启美颜");
            getCommonRender().closeBeauty();
        } else {
            mBeautyIsOpen = true;
            btnBeauty.setText("关闭美颜");
            getCommonRender().openBeauty();
            ;
        }
    }

    private void openOrCloseCamera() {
        if (mCameraIsOpen) {
            mCameraIsOpen = false;
            getCommonRender().closeCamera();
            btnOpenCamera.setText("打开相机");

        } else {
            mCameraIsOpen = true;
            getCommonRender().openCamera(mSurfaceView.getHeight(), mSurfaceView.getWidth());
            btnOpenCamera.setText("关闭相机");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getCommonRender().closeCamera();
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
                getCommonRender().startRecord(1.f);
                btnRecord.setBackground(getDrawable(R.drawable.bg_btn_circle_green));
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "onTouch ACTION_UP");
            case MotionEvent.ACTION_CANCEL:
                getCommonRender().stopRecord();
                btnRecord.setBackground(getDrawable(R.drawable.bg_btn_circle_red));
                break;
        }

        return false;
    }

    private CommonRender getCommonRender() {
        return mSurfaceView.getRender();
    }
}
