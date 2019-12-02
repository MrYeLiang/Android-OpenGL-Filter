package com.yeliang.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.yeliang.R;
import com.yeliang.utils.FileUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Author: yeliang
 * Date: 2019/12/2
 * Time: 3:46 PM
 * Description:
 */

public class ScreenFilter {

    private FloatBuffer mTextureBuffer;
    private int mShaderProgram;

    private int vPosition;
    private int vCoord;
    private int vMatrix;
    private int vTexture;

    private FloatBuffer mVertexBuffer;
    private int mHeight;
    private int mWidth;


    public ScreenFilter(Context context) {
        String vertexSource = FileUtils.readRawTextFile(context, R.raw.camera_vertex);
        String fragSource = FileUtils.readRawTextFile(context, R.raw.camera_frag);

        //1 顶点着色器
        int vShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShaderId, vertexSource);
        GLES20.glCompileShader(vShaderId);

        int[] status = new int[1];

        GLES20.glGetShaderiv(vShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 顶点着色器编译失败");
        }

        //2 片元着色器
        int fShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShaderId, fragSource);
        GLES20.glCompileShader(fShaderId);

        GLES20.glGetShaderiv(fShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 片元着色器编译失败!");
        }

        //3 创建着色器程序
        mShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mShaderProgram, vShaderId);
        GLES20.glAttachShader(mShaderProgram, fShaderId);

        GLES20.glLinkProgram(mShaderProgram);

        GLES20.glGetProgramiv(mShaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 着色器程序配置失败");
        }

        GLES20.glDeleteShader(vShaderId);
        GLES20.glDeleteShader(fShaderId);

        //4 获取属性
        vPosition = GLES20.glGetAttribLocation(mShaderProgram, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mShaderProgram, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mShaderProgram, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(mShaderProgram, "vTexture");


        //5.1 顶点坐标
        mVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.clear();
        float[] vertexCoord = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
        };
        mVertexBuffer.put(vertexCoord);

        //5.2 纹理坐标
        mTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuffer.clear();

        //旋转+镜像 之后
        float[] textureCoord = {
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 1.0f
        };
        mTextureBuffer.put(textureCoord);
    }

    public void onDrawFrame(int texture, float[] mtx) {

        //1 设置窗口大小
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glUseProgram(mShaderProgram);


        //2 传递顶点/纹理坐标
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);


        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onReady(int width, int height){
        mWidth = width;
        mHeight = height;
    }
}
