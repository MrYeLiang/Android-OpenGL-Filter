precision mediump float;

varying vec2 aCoord;

uniform sampler2D vTexture;

uniform vec2 left_eye;//左眼
uniform vec2 right_eye;//右眼

float fs(float r, float rmax){
    float a = 0.4;
    return (1.0 - pow((r / rmax - 1.0), 2.0) * a);
}
//coord:输入采样点 eye:眼睛坐标点  rmax:最大作用半径
//这个方法的目的是重新返回一个采样点的坐标，来实现放大效果
vec2 newCoord(vec2 coord, vec2 eye, float rmax){
    vec2 new_coord = coord;

    //1 算出当前采样点的坐标与眼睛的距离
    float r = distance(coord, eye);

    //如果在作用半径范围内
    if(r < rmax){
        //2 套用放大公式 即根据 当前采样点与眼睛的距离  和  作用半径 算出一个系数
        float fsr = fs(r, rmax);

        //3 用这个系数来求得新坐标点的位置 new_coord - eye = fsr*(coord - eye)
        new_coord = fsr * (coord - eye) + eye;
    }

    return new_coord;
}

void main() {
    //1 计算放大半径
    float rmax = distance(left_eye, right_eye) / 2.0;

    //2.1  计算左眼放大处理后的采样点坐标
    vec2 new_coord = newCoord(aCoord, left_eye, rmax);

    //2.2  计算右眼放大处理后的采样点坐标
    new_coord = newCoord(new_coord, right_eye, rmax);

    //3 将重新求得的采样点坐标传给片元着色器
    gl_FragColor = texture2D(vTexture, new_coord);

}
