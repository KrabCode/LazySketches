#version 460

precision highp float;
precision highp int;

out vec4 fragColor;

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float xor(ivec2 p){
    int modulo = int(time);
    return (p.y ^ p.x) % modulo;
}

float render(vec2 uv){
    float reps = 1024.;
    ivec2 p = ivec2(abs(floor(uv * reps) + 0.5));
    float xor = xor(p);
    float result = clamp(xor, 0, 1);
    return result;
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 clrA = vec3(0.8);
    vec3 clrB = vec3(0.1);
    vec3 fc = mix(clrA, clrB, render(uv));
    fragColor = vec4(fc, 1.);
}