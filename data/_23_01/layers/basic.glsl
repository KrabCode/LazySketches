uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = 0.5 + 0.5*cos(time+uv.xyx+vec3(0,2,4));
    vec3 pic = texture(texture, gl_FragCoord.xy / resolution.xy).rgb;
    vec3 outColor = mix(col, pic, 0.75);
    gl_FragColor = vec4(outColor, 1.);
}