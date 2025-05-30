#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 texCoord;
uniform samplerExternalOES sTexture;
uniform int uMirror; // 镜像开关

void main() {
    vec2 coord = texCoord;
    if (uMirror == 1) {
        coord.x = 1.0 - coord.x; // 水平镜像 :cite[2]
    }
    gl_FragColor = texture2D(sTexture, coord);
}