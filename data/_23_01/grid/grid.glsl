uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

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