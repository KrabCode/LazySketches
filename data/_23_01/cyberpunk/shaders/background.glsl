
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform vec3 rgbA;
uniform vec3 rgbB;

vec4 hash44(vec4 p4)
{
    p4 = fract(p4  * vec4(.1031, .1030, .0973, .1099));
    p4 += dot(p4, p4.wzxy+33.33);
    return fract((p4.xxyz+p4.yzzw)*p4.zywx);
}

void main(){
    vec2 uv = (gl_FragCoord.xy - resolution.xy * 0.5) / resolution.y;
    float y = -uv.y+uv.x*3.;
    float yNorm = smoothstep(-.45, 0.75, y);
    vec3 col = mix(rgbA, rgbB, yNorm);
    gl_FragColor = vec4(col, 1.);
}