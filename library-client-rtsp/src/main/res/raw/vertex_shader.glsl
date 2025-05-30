attribute vec4 vPosition;
attribute vec2 vTexCoord;
varying vec2 texCoord;
uniform mat4 uTexMatrix;

void main() {
    gl_Position = vPosition;
    texCoord = (uTexMatrix * vec4(vTexCoord, 0, 1)).xy;
}