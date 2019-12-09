package com.yeliang.utils;

import android.opengl.GLES20;

/**
 * Author: yeliang
 * Date: 2019/12/7
 * Time: 2:12 PM
 * Description:
 */

public class OpenGlUtils {

    /**
     * 创建并配置纹理
     * @param textures 被创建的纹理id
     */
    public static void glConfigureTextures(int[] textures) {
        GLES20.glGenTextures(textures.length, textures, 0);

        for (int texture : textures) {
            //1 绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

            //2 设置纹理过滤样式 GL_NEAREST:临近过滤，清晰看到组成纹理的像素但有颗粒感
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

            //3 设置纹理环绕方式 当纹理坐标超过默认范围时 GL_REPEAT:重复纹理图像
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            //2 解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    /**
     * 根据顶点着色器和片元着色器代码来创建着色器程序
     * @param vSource 顶点着色器代码
     * @param fSource 片元着色器代码
     * @return 着色器program
     */
    public static int loadProgram(String vSource, String fSource) {

        //1 顶点着色器
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vSource);
        GLES20.glCompileShader(vShader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("load vertex shader failed: " + GLES20.glGetShaderInfoLog(vShader));
        }

        //2 片元着色器
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fSource);
        GLES20.glCompileShader(fShader);
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("load fragment shader:" + GLES20.glGetShaderInfoLog(vShader));
        }

        //3 着色器程序
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("link program failed: " + GLES20.glGetProgramInfoLog(program));
        }
        GLES20.glDeleteShader(vShader);
        GLES20.glDeleteShader(fShader);
        return program;
    }
}
