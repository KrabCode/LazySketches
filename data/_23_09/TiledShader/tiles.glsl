uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
// taken from this youtube tutorial:
// https://www.youtube.com/watch?v=f4s1h2YETNY

vec3 palette(float t)
{
    vec3 a = vec3(0.5);
    vec3 b = vec3(0.5);
    vec3 c = vec3(1.);
    vec3 d = vec3(0.0, 0.1, 0.2);
    return a + b * cos(6.28318*(c*t+d));
}

vec3 render(vec2 uv){
    vec2 uv0 = uv;
    float d0 = length(uv0);
    vec3 finalColor = vec3(0.0);
    float t = time * 0.05;
    for(float i = 0.; i < 6.0; i++){
        float tileCount = 1.15;
        uv = fract(tileCount * uv) - 0.5;
        float d = length(uv) * exp(-d0);
        vec3 col = palette(d0 + i * 0.05 + t * 0.5);
        float rep = 4.2;
        d = sin(d * rep + t) / rep;
        d = abs(d);
        d = pow(0.015 / d, 1.5);
        finalColor += col * d;
    }
    finalColor *= smoothstep(1.0, 0.0, d0);
//    finalColor = max(vec3(0.05), finalColor);
    return finalColor;
}

vec3 aaRender(vec2 uv){
    vec2 offset = vec2(0.3/resolution.xy);
    return (render(uv + offset * vec2(-1, -1)) +
            render(uv + offset * vec2(+1, -1)) +
            render(uv + offset * vec2(+1, +1)) +
            render(uv + offset * vec2(-1, +1))) / 4.;
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution.xy) / resolution.y;
    vec3 clr = aaRender(uv);
    gl_FragColor = vec4(clr, 1.);
}