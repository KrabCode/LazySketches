#version 460

precision highp float;
precision highp int;

out vec4 fragColor;

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float xor(ivec2 p, int mod){
    return ((p.x + p.y) & (p.y | p.x)) % int(time/p.y);
}

float render(vec2 uv){
    float result = 0;
    float reps = 256;
    float amt = 0.002;
    for(int i = 2; i < 6; i++){
        ivec2 p = ivec2(abs(floor(uv * reps) + 0.5));
        result += amt * clamp(xor(p, i), 0, 100);
        reps *= 2.;
    }
    return 1.-clamp(result, 0., 1.);
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 clrA = vec3(1.);
    vec3 clrB = vec3(0.);
    vec3 fc = mix(clrA, clrB, render(uv));
    fc = pow(fc, vec3(1./2.2));
    fragColor = vec4(fc, 1.);
}