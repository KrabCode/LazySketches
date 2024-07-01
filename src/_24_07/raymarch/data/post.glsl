
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec4 col = texture(texture, uv).rgba;
//    col *= 1.0;
    gl_FragColor = col;
}