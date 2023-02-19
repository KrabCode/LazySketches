
uniform sampler2D histogram;
uniform sampler2D palette;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 histoSample = texture(histogram, uv).rgb;
    float br = histoSample.r;
    br = pow(br, 1./2.2);
    vec3 clr = texture(palette, vec2(0.5, br)).rgb;
    gl_FragColor = vec4(clr, 1.);
}