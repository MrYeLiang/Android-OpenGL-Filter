precision mediump float;

varying mediump vec2 aCoord;

uniform sampler2D vTexture;

uniform int width;
uniform int height;

vec2 blurCoordinates[20];
void main() {
    //1 平滑处理
    vec2 singleStepOffset = vec2(1.0/float(width), 1.0/float(height));

    blurCoordinates[0] =  aCoord.xy + singleStepOffset * vec2(0.0, -10.0);
    blurCoordinates[1] =  aCoord.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] =  aCoord.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] =  aCoord.xy + singleStepOffset * vec2(10.0, 0.0);

    blurCoordinates[4] =  aCoord.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] =  aCoord.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] =  aCoord.xy + singleStepOffset * vec2(-5.0,8.0);
    blurCoordinates[7] =  aCoord.xy + singleStepOffset * vec2(-5.0, -8.0);

    blurCoordinates[8] =  aCoord.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] =  aCoord.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = aCoord.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[11] = aCoord.xy + singleStepOffset * vec2(-8.0, 5.0);

    blurCoordinates[12] = aCoord.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = aCoord.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = aCoord.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = aCoord.xy + singleStepOffset * vec2(-6.0, 0.0);

    blurCoordinates[16] = aCoord.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = aCoord.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = aCoord.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = aCoord.xy + singleStepOffset * vec2(4.0, 4.0);

    //2 计算当前点的像素值
    vec4 currentColor = texture2D(vTexture, aCoord);
    vec3 rgb = currentColor.rgb;

    //3 计算偏移坐标颜色值总和
    for(int i = 0; i < 20; i++ ){
        //采集20个点的像素值相加，得到总和
        rgb += texture2D(vTexture, blurCoordinates[i].xy).rgb;
    }

    //4 计算21个点的平均rgba值
    vec4 blurColor = vec4(rgb * 1.0 / 21.0, currentColor.a);

    //5 计算高反差值  clamp ---> 获取三个参数中的中间值
    vec4 highPassColor = currentColor - blurColor;
    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * 24.0, 0.0, 1.0);
    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * 24.0, 0.0, 1.0);
    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * 24.0, 0.0, 1.0);

    //6 过滤疤痕
    vec4 highPassBlur = vec4(highPassColor.rgb, 1.0);

    //7 融合 ---> 磨皮
    float blue = min(currentColor.b, blurColor.b);
    float value = clamp((blue - 0.2) * 5.0, 0.0, 1.0);

    //8 求RGB的最大值
    float maxChannelColor = max(max(highPassBlur.r, highPassBlur.g), highPassBlur.b);

    //9 计算模糊等级
    float intensity = 1.0;
    float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * intensity;

    //10 融合
    vec3 r = mix(currentColor.rgb, blurColor.rgb, currentIntensity);

    gl_FragColor = vec4(r, 1.0);
}