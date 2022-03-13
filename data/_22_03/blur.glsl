
uniform sampler2D texture;
uniform vec2 resolution;
uniform float distance;

void main() {
    vec4 leftPixel = texture(texture,vec2(gl_FragCoord.xy/resolution.xy - vec2(1,0)*distance/resolution.xy ));
    vec4 upPixel =   texture(texture,vec2(gl_FragCoord.xy/resolution.xy + vec2(0,1)*distance/resolution.xy ));
    vec4 downPixel = texture(texture,vec2(gl_FragCoord.xy/resolution.xy - vec2(0,1)*distance/resolution.xy ));
    vec4 rightPixel = texture(texture,vec2(gl_FragCoord.xy/resolution.xy + vec2(1,0)*distance/resolution.xy ));
    vec3 composite = (leftPixel.rgb + upPixel.rgb + downPixel.rgb + rightPixel.rgb) / 4.;
    gl_FragColor = vec4(composite, 1.);
}
