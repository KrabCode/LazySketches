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

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution.xy) / resolution.y;
    vec2 uv0 = uv;
    vec3 finalColor = vec3(0.0);
    float t = time;
    for(float i = 0; i < 5.0; i++){
        float tileCount = 1.5;
        uv = fract(tileCount * uv) - 0.5;
        float d = length(uv) * exp(-length(uv0));
        vec3 col = palette(length(uv0) + i * 0.05 + t * 0.5);
        float rep = 8.;
        d = sin(d * rep + t) / rep;
        d = abs(d);
        d = pow(0.01 / d, 1.2);
        finalColor += col * d;
    }
    gl_FragColor = vec4(finalColor, 1.);
}