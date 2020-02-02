package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yeliang.R;
import com.yeliang.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by yeliang on 2020/1/18.
 */

public class BigEyeFilter extends BaseFrameFilter {
    private int left_eye;
    private int right_eye;

    private FloatBuffer left;
    private FloatBuffer right;

    private Face mFace;

    public BigEyeFilter(Context context) {
        super(context, R.raw.screen_vertex, R.raw.bigeye_frag);

        //参数索引
        left_eye = GLES20.glGetUniformLocation(mGLProgramId, "left_eye");
        right_eye = GLES20.glGetUniformLocation(mGLProgramId, "right_eye");

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public void setFace(Face face) {
        mFace = face;
    }

    @Override
    protected void initCoordinate() {
        mGLTextureBuffer.clear();

        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLTextureBuffer.put(TEXTURE);
    }

    @Override
    public int onDrawFrame(int textureId) {
        if (null == mFace) {
            return textureId;
        }

        //1 设置窗口大小
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //2 绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //3 使用着色器
        GLES20.glUseProgram(mGLProgramId);

        //4 传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0 , mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        float[] landmarks = mFace.landmarks;

        //5.1 左眼坐标点赋值
        float x = landmarks[2] / mFace.imgWidth;
        float y = landmarks[3] / mFace.imgHeight;

        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        GLES20.glUniform2fv(left_eye, 1, left);

        //5.2 右眼坐标点赋值
        x = landmarks[4] / mFace.imgWidth;
        y = landmarks[5] / mFace.imgHeight;

        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        GLES20.glUniform2fv(right_eye, 1, right);

        //6 激活并绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        //7 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //8 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBufferTextures[0];
    }
}
