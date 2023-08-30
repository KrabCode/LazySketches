precision highp float;
precision highp sampler2D;

uniform sampler2D texture;
uniform vec2 resolution;

uniform float dA;
uniform float dB;
uniform float f;
uniform float k;
uniform float t;

// Based on https://karlsims.com/rd.html
// A is represented by RED
// green is unused
// B is represented by BLUE


vec2 getValuesFromColor(vec4 col){
    return vec2(col.r, col.b);
}

vec4 setValuesFromColor(vec2 vars){
    return vec4(vars.x, 0., vars.y, 1);
}

vec4 getPixel(vec2 pos){
    return texture2D(texture, vec2(pos));
}

vec2 getPixelValues(vec2 pos){
    return getValuesFromColor(getPixel(pos));
}

vec2 getLaplacianAverageValues(vec2 p){
    vec2 step = vec2(1) / resolution.xy;
    return
        getPixelValues(p + step*vec2(+0, -1)) * 0.2 +
        getPixelValues(p + step*vec2(+0, +1)) * 0.2 +
        getPixelValues(p + step*vec2(-1, +0)) * 0.2 +
        getPixelValues(p + step*vec2(+1, +0)) * 0.2 +
        getPixelValues(p + step*vec2(+1, +1)) * 0.05 +
        getPixelValues(p + step*vec2(+1, -1)) * 0.05 +
        getPixelValues(p + step*vec2(-1, +1)) * 0.05 +
        getPixelValues(p + step*vec2(-1, -1)) * 0.05;
}

vec2 simulateReactionDiffusion(vec2 p){
    vec2 AB = getPixelValues(p);
    float A = AB.x;
    float B = AB.y;
    vec2 lapDiff = - AB + getLaplacianAverageValues(p);
    float ABsquared = A * B * B;
    float newA = A + (dA * lapDiff.x - ABsquared + f * (1.0 - A)) * t;
    float newB = B + (dB * lapDiff.y + ABsquared - ((k + f) * B)) * t;
    return clamp(vec2(newA, newB), vec2(0), vec2(1));
}

void main(){
    vec2 p = vec2(gl_FragCoord.xy / resolution.xy);
    vec2 newValues = simulateReactionDiffusion(p);
    gl_FragColor = setValuesFromColor(newValues);
}