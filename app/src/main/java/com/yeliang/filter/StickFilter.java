package com.yeliang.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.yeliang.R;
import com.yeliang.face.Face;
import com.yeliang.utils.OpenGlUtils;

/**
 * Author: yeliang
 * Date: 2020-02-04
 * Time: 08:14
 * Description:
 */
public class StickFilter extends BaseFrameFilter {

    private Bitmap mBitmap;
    private int[] mTextureId;
    private Face mFace;

    public StickFilter(Context context) {
        super(context, R.raw.screen_vertex, R.raw.screen_frag);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stick_ear);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        //1 创建纹理id
        mTextureId = new int[1];

        //2 配置纹理
        OpenGlUtils.glConfigureTextures(mTextureId);

        //3 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        //4 将bitmap与纹理id绑定到一起
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        //5 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void setFace(Face face) {
        mFace = face;
    }

    @Override
    public int onDrawFrame(int textureId) {
        if (null == mFace) {
            return textureId;
        }

        //1 设置窗口大小
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //2 bind fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //3 使用着色器
        GLES20.glUseProgram(mGLProgramId);

        //4.1 传递顶点坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //4.2 传递纹理坐标
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //5 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        //6 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //7 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        onDrawStick();
        return mFrameBufferTextures[0];
    }

    private void onDrawStick() {
        //1 开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);

        //2 设置贴图模式
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //3 获取人脸的起始位置
        float x = mFace.landmarks[0];
        float y = mFace.landmarks[1];

        //4 转换为要画到屏幕上的宽高
        x = x / mFace.imgWidth * mOutputWidth;
        y = y / mFace.imgHeight * mOutputHeight;

        //5 设置窗口大小为人脸的宽高
        GLES20.glViewport(
                (int) x,
                (int) y - mBitmap.getHeight() / 2,
                (int) ((float) mFace.width / mFace.imgWidth * mOutputWidth),
                mBitmap.getHeight());

        //6 绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //7 使用程序
        GLES20.glUseProgram(mGLProgramId);

        //8.1 传递顶点坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //8.2 传递纹理坐标
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //9 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLES20.glUniform1i(vTexture, 0);

        //10 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //11 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //12 关闭混合模式
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    protected void initCoordinate() {
        mGLTextureBuffer.clear();
        //从opengl画到opengl 不是画到屏幕， 修改坐标
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLTextureBuffer.put(TEXTURE);
    }

    @Override
    public void release() {
        super.release();
        mBitmap.recycle();
    }
}
