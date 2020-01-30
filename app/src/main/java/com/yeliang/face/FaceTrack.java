package com.yeliang.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.yeliang.utils.CameraHelper;

/**
 * Created by yeliang on 2020/1/16.
 */

public class FaceTrack {
    static {
        System.loadLibrary("native-lib");
    }

    private CameraHelper mCameraHelper;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private long self;

    private Face mFace;

    public void setCameraHelper(CameraHelper cameraHelper) {
        mCameraHelper = cameraHelper;
    }

    public FaceTrack(String model, String seeta) {
        self = native_create(model, seeta);

        Log.i("detector", "self = " + self);
        mHandlerThread = new HandlerThread("track");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                synchronized (FaceTrack.this) {

                    Log.i("detector A", "self = " + self + ", msg.obj" + msg.obj.toString() + ", cameraId = " + mCameraHelper.getCameraId());
                    mFace = native_detector(self, (byte[]) msg.obj, mCameraHelper.getCameraId(), CameraHelper.WIDTH, CameraHelper.HEIGHT);

                    Log.i("detector B", "Face = " + mFace);
                }
            }
        };
    }

    public void startTrack() {
        native_start(self);
    }

    public void stopTrack() {
        synchronized (this) {
            mHandlerThread.quitSafely();
            mHandler.removeCallbacksAndMessages(null);
            native_stop(self);
            self = 0;
        }
    }

    public void detecor(byte[] data) {
        mHandler.removeMessages(11);

        Message message = mHandler.obtainMessage(11);
        message.obj = data;
        mHandler.sendMessage(message);
    }

    public Face getFace() {
        return mFace;
    }

    private native long native_create(String model, String seeta);

    private native void native_start(long self);

    private native void native_stop(long self);

    private native Face native_detector(long self, byte[] data, int cameraId, int width, int height);
}
