#define PROCESSING_LIGHT_SHADER

uniform mat4 modelview;
uniform mat4 transform;
uniform mat3 normalMatrix;

attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

varying vec3 vEcPos;
varying vec3 vEcNormal;
varying vec4 vColor;
varying float vHeight;

void main() {
    gl_Position = transform * position;
    vEcPos = vec3(modelview * position);
    vEcNormal = normalize(normalMatrix * normal);
    vColor = color;
    vHeight = position.y;
}
