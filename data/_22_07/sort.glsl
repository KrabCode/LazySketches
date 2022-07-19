uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float speed;

vec3 get(vec2 uv){
    return texture(texture, uv / resolution.xy).rgb;
}

void main(){
    vec2 p = gl_FragCoord.xy;
    vec3 center = get(p);
    vec3 top = get(p + vec2(0, -1));
    vec3 bot = get(p + vec2(0, 1));
    vec3 clr = mix(top, bot, clamp(speed, 0., 1.));
    gl_FragColor = vec4(clr, 1.);
}