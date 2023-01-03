
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

vec4 hash44(vec4 p4)
{
    p4 = fract(p4  * vec4(.1031, .1030, .0973, .1099));
    p4 += dot(p4, p4.wzxy+33.33);
    return fract((p4.xxyz+p4.yzzw)*p4.zywx);
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
//    uv.y = 1.-uv.y;
    float t = time;
    vec3 n = hash44(vec4(uv.xy*10.1, t, -t)).rgb;
    float a = texture(texture, uv).a;
    vec3 rgb = texture(texture, uv).rgb;
    vec3 mixed = max(rgb, smoothstep(0.89, 0.95, n));
    gl_FragColor = vec4(mixed, a);
}