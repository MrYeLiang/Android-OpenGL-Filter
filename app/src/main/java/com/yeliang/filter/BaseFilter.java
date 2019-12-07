package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yeliang.utils.FileUtils;
import com.yeliang.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Author: yeliang
 * Date: 2019/12/7
 * Time: 11:50 AM
 * Description:
 */

public abstract class BaseFilter {

    FloatBuffer mGLVertexBuffer;
    FloatBuffer mGLTextureBuffer;

    private int mVertexShaderId;
    private int mFragShaderId;

    int mGLProgramId;

    int vPosition;
    int vCoord;
    int vMatrix;
    int vTexture;

    int mOutputWidth;
    int mOutputHeight;

    BaseFilter(Context context, int vertexShaderId, int fragShaderId) {
        mVertexShaderId = vertexShaderId;
        mFragShaderId = fragShaderId;

        mGLVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLVertexBuffer.clear();
        float[] vertex = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLVertexBuffer.put(vertex);

        mGLTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLTextureBuffer.clear();
        float[] texture = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };
        mGLTextureBuffer.put(texture);

        initShader(context);

        initCoordinate();
    }

    //获取着色器中的属性
    private void initShader(Context context) {
        String vertexShader = FileUtils.readRawTextFile(context, mVertexShaderId);
        String fragShader = FileUtils.readRawTextFile(context, mFragShaderId);

        mGLProgramId = OpenGlUtils.loadProgram(vertexShader, fragShader);
        vPosition = GLES20.glGetAttribLocation(mGLProgramId, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mGLProgramId, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mGLProgramId, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(mGLProgramId, "vTexture");
    }

    //给着色器中的属性赋值
    protected void initCoordinate(){
    }

    public void onReady(int width, int height) {
        mOutputHeight = height;
        mOutputWidth = width;
    }

    public abstract int onDrawFrame(int textureId);

    public void release() {
        GLES20.glDeleteProgram(mGLProgramId);
    }
}
