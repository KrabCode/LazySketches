uniform sampler2D canvas;
uniform vec2 resolution;
uniform float time;

uniform float strength;
uniform float height;
uniform vec3 lightDir;

float col(float x, float y){
    vec2 uv = (gl_FragCoord.xy + vec2(x, y)) / resolution.xy;
    vec3 col = texture(canvas, uv).rgb;
    float br = (col.r + col.g + col.b) / 3.;
    return br;
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 col = texture(canvas, uv).rgb;
    float lit = dot(col, normalize(lightDir));
    gl_FragColor = vec4(vec3(lit), 1.);
}