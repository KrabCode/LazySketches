#version 120
#define tau 6.28
precision highp int;

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float layer;
uniform vec3 rectSize;
uniform vec3 rectPos;
uniform float layerFreq;
uniform float layerAmp;
uniform vec3 layerPosXY;

uniform float blurOpaque;
uniform float blurShine;
uniform vec3 glowColor;
uniform vec3 rectColor;

float sdBox( in vec2 p, in vec2 b ){
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

float foreRect(vec2 uv, float n, bool absolute){
    float t = time*0.1;
    vec2 rectSize = vec2(0.3, 0.05);
    float nx = 0.1;
    float ny = -0.15;
    vec2 pos = vec2(0., -0.5*ny+ny*n + 0.01*sin(uv.x*26. + (0.2+n)*t + n*tau));
    float box = sdBox(uv-pos, rectSize);
    float edgeBlurPixels = 5.;
    if(absolute){
        box = abs(box);
        edgeBlurPixels = blurShine;
    }
    float normBox = clamp(smoothstep(edgeBlurPixels / resolution.y, 0., box), 0., 1.);
    return normBox;
}

void main(){
    vec2 uv = (gl_FragCoord.xy - .5 * resolution) / resolution.y;
    vec3 pRGB = texture2D(texture, gl_FragCoord.xy / resolution.xy).rgb;
    vec3 col = vec3(pRGB);
    float rect = foreRect(uv, layer, false);
    col.rgb += foreRect(uv, layer, true);
    col.rgb *= smoothstep(1., 0.0, rect);
    gl_FragColor = vec4(col, 1.);
}