package com.yeliang.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;

import com.yeliang.utils.CameraHelper;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Author: yeliang
 * Date: 2019/12/10
 * Time: 8:28 PM
 * Description:
 */

public class MediaRecorder {

    private final Context mContext;
    private final String mPath;
    private final EGLContext mEglContext;

    private int mWidth;
    private int mHeight;

    private float mSpeed;

    private MediaCodec mMediaCodec;
    private MediaMuxer mMediaMuxer;
    private Surface mInputSurface;
    private Handler mHandler;
    private EGLBase mEglBase;
    private boolean isStart;
    private int index;

    public MediaRecorder(Context context, String path, EGLContext eglContext) {
        mContext = context;
        mPath = path;
        mEglContext = eglContext;
    }

    public void start(float speed) throws IOException {
        mWidth = CameraHelper.mHeight;
        mHeight = CameraHelper.mWidth;

        if (mWidth == 0) {
            mWidth = 1080;
        }
        if (mHeight == 0) {
            mHeight = 1920;
        }


        mSpeed = speed;

        //1 配置视频参数
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1500_000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 20);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        //2 创建并配置解码器
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        //3 创建surface
        mInputSurface = mMediaCodec.createInputSurface();

        //4 创建解封装器
        mMediaMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        //5 配置EGL线程环境
        HandlerThread handlerThread = new HandlerThread("VideoCodec");
        handlerThread.start();

        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);

        //EGL绑定线程，
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //5.1 创建EGL环境
                mEglBase = new EGLBase(mContext, mWidth, mHeight, mInputSurface, mEglContext);
                //5.2 启动编码器
                mMediaCodec.start();
                isStart = true;
            }
        });

    }

    public void encodeFrame(final int textureId, final long timeStamp) {
        if (!isStart) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEglBase.draw(textureId, timeStamp);
                getCodec(false);
            }
        });
    }

    private void getCodec(boolean endOfStream) {
        if (endOfStream) {
            mMediaCodec.signalEndOfInputStream();
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (true) {
            int status = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {

                if (!endOfStream) {
                    break;
                }

            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                MediaFormat outputFormat = mMediaCodec.getOutputFormat();
                index = mMediaMuxer.addTrack(outputFormat);
                mMediaMuxer.start();

            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

            } else {

                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(status);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs / mSpeed);
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    mMediaMuxer.writeSampleData(index, outputBuffer, bufferInfo);
                }

                mMediaCodec.releaseOutputBuffer(status, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }

            }

        }
    }

    public void stop() {
        isStart = false;
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getCodec(true);
                    mMediaCodec.stop();
                    mMediaCodec.release();
                    mMediaCodec = null;

                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                    mMediaMuxer = null;

                    mEglBase.release();
                    mEglBase = null;

                    mInputSurface = null;
                    mHandler.getLooper().quitSafely();
                    mHandler = null;
                }
            });
        }
    }
}
