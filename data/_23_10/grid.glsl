uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

mat2 rotate2D(float a){
    return mat2(cos(a), sin(a), -sin(a), cos(a));
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = vec3(0.);
    float reps = 10.;
    vec2 gv = (fract(uv * reps)-0.5);
    vec2 id = floor(uv * reps)+0.5;
    col.r = length(id) * 0.1;
    float square = max(abs(gv.x), abs(gv.y))*0.5+0.25;
    square =
    col.b += square;
    gl_FragColor = vec4(col, 1.);
}

