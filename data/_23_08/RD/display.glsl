
uniform sampler2D source;
uniform sampler2D gradient;
uniform vec2 resolution;
uniform bool displayRedAsWhite = false;

vec3 getColorFromGradient(float pos){
    return texture(gradient, vec2(0.5, 1.-pos)).rgb;
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec4 data = texture(source, uv).rgba;
    float val = 1.0-data.b;
    vec3 gradientOutput = getColorFromGradient(val);
    gl_FragColor = vec4(gradientOutput, 1);
}