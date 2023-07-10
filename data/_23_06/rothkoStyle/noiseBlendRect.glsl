uniform sampler2D texture;
uniform vec2 resolution;
uniform vec2 rectPos;
uniform float boxSmoothLow;
uniform float boxSmoothHigh;
uniform vec2 rectSize;
uniform vec3 fill;
uniform float time;

float sdBox( vec2 p, vec2 b )
{
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = fill;
    vec2 p = gl_FragCoord.xy;
    p.y = resolution.y - p.y;
    float box = sdBox((rectPos-p+rectSize/2.)/resolution.xy, (rectSize/2.)/resolution.xy);
    float s = smoothstep(box, boxSmoothLow, boxSmoothHigh);
//    col.rgb = vec3(s);

    gl_FragColor = vec4(col, s);
}