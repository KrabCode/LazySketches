#version 120
#extension GL_EXT_gpu_shader4 : enable

precision highp float;
precision highp int;

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float render(vec2 uv){
    float reps = 256.;
    vec2 p = abs(floor(uv * reps) + 0.5);
    ivec2 ip = ivec2(p);
    int xor = (ip.y ^ ip.x) % 7;
    float result = 1. - min(1, xor);
    return result;
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 fc = vec3(0.12);
    fc += render(uv);
    gl_FragColor = vec4(fc, 1.);
}