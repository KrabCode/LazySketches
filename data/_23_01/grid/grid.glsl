
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

const int octaves = 4;
const float pi = 3.14159;


float modifier(vec2 p, float d, float i, float t){
    return clamp(0.75+0.5*sin(d*2. + t + i*1.5), 0., 1.);
}

float line(vec2 p, float w, float cd, float i, float t){
    float d = min(length(p.x), length(p.y));
    return smoothstep(w, 0., d) * modifier(p, cd, i, t);
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy - resolution.xy * 0.5) / resolution.y;
    cv *= 10.;
    float w = 0.03;
    float cd = length(cv);
    float t = -time;
    float pct = line(cv, w, cd, 0, t);
    for(float i = 0; i < octaves; i++){
        float scale = pow(2., i);
        vec2 gridPos = fract(cv * scale) - 0.5;
        float line = line(gridPos, w, cd, i, t);
        pct = max(line, pct);
    }
    gl_FragColor = vec4(vec3(pct), 1.);
}