
uniform sampler2D texture;
uniform vec2 resolution;
uniform int frame;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float sign = mod(frame, 2) == 0 ? -1 : 1;
    uv += sign * (0.5 / resolution.xy);
    vec4 col = texture(texture, uv).rgba;
    gl_FragColor = col;
}