uniform sampler2D texture;
uniform sampler2D image;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    uv.y = 1.-uv.y;
    vec3 col = 0.5 + 0.5*cos(time+cv.xyx+vec3(0,2,4));
    vec3 pic = texture(image, uv).rgb;
    vec3 outColor = mix(col, pic, 0.75);
    gl_FragColor = vec4(outColor, 1.);
}