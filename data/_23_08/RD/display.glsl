
uniform sampler2D img;
uniform vec2 resolution;
uniform bool displayRedAsWhite = false;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec4 col = texture(img, uv).rgba;
    if(displayRedAsWhite){
        col.rgb = vec3(col.r);
    }else{
        col.rgb = vec3(col.b);
    }
    gl_FragColor = vec4(col.rgb, 1);
}