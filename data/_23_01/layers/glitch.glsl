uniform sampler2D texture;
uniform sampler2D image;
uniform vec2 resolution;
uniform float time;

vec4 hash44(vec4 p4)
{
    p4 = fract(p4  * vec4(.1031, .1030, .0973, .1099));
    p4 += dot(p4, p4.wzxy+33.33);
    return fract((p4.xxyz+p4.yzzw)*p4.zywx);
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    //    vec3 col = 0.5 + 0.5*cos(time+cv.xyx+vec3(0,2,4));
    vec3 pic = texture(image, vec2(uv.x, 1.-uv.y)).rgb;
    vec3 tex = texture(texture, uv).rgb;
    float texel = 1. / max(resolution.x, resolution.y);
    vec2 off = texel*vec2(0.5+0.2*cos(time), 0.01*sin(time*5.5));
    vec3 clr = texture(texture, uv+off).rgb;

    vec2 floor = gl_FragCoord.xy;
    vec4 p = vec4(floor, cos(time), sin(time));
    float n = length(hash44(p));
    n = smoothstep(0.2, 0.8, n);

    clr = mix(pic, clr, clamp(n, 0., 1.));
//    clr -= 0.01*n;
//    clr = pic;
    gl_FragColor = vec4(clr, 1.);
}