
uniform sampler2D histogram;
uniform sampler2D palette;
uniform vec2 resolution;
uniform float time;
uniform float logMax;
uniform float gammaPow;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 histoSample = texture(histogram, uv).rgb;
    float br = histoSample.r;
    float a = log(br) / log(logMax);
    a = pow(a, 1./gammaPow);
    vec3 clr = texture(palette, vec2(0.5, a)).rgb;
    gl_FragColor = vec4(clr, 1.);
}