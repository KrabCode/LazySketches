
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

// based on code from here
// https://gist.github.com/983/e170a24ae8eba2cd174f

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

/*
    // color shift
    vec3 hsv = rgb2hsv(fragColor.rgb);
    hsv.x = fract(hsv.x + iMouse.x / iResolution.x);
    fragColor.rgb = hsv2rgb(hsv);
*/

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy - resolution.xy *0.5) / resolution.y;
    vec4 col = texture(texture, uv).rgba;
    vec3 hsv = rgb2hsv(col.rgb);
//    hsv.x += mod(length(uv.xy)*1.5+time*0.5, 1.);
    float d = length(cv+0.3)*1.5;
    float t = time * 1;
    hsv.x = mod(hsv.x - d + t*0.5, 1.);
    col.rgb = hsv2rgb(hsv);
    gl_FragColor = col;
}