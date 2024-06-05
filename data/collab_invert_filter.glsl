
uniform sampler2D tex0;
uniform sampler2D tex1;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;

    vec4 col =    texture(tex0, uv);

    gl_FragColor = mix(
    col,
    1.-col,
    texture(tex1, uv).r
    );
}