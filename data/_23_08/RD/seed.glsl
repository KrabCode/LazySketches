
uniform sampler2D texture;
uniform vec2 resolution;
uniform float n;

void main(){
    vec2 uv = (gl_FragCoord.xy - resolution.xy * 0.5) / resolution.y;
    float redVal = 1.-step(length(uv), n);
    vec3 col = vec3(1,0,redVal);
    gl_FragColor = vec4(col.rgb, 1);
}