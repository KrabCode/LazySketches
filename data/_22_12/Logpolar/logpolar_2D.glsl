

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float rotationTime;
uniform float scalingTime;
uniform int copies;

#define PI 3.14159

vec2 cartesianToLogPolar(vec2 p){
    return vec2(log(length(p)), atan(p.y, p.x));
}

vec2 logPolarToCartesian(vec2 p){
    return vec2(exp(p.x) * cos(p.y), exp(p.x) * sin(p.y));
}

// sd functions shamelessly copied from inigo quilez
// https://iquilezles.org/articles/distfunctions2d/
float sdCutDisk( vec2 p, float r, float h )
{
    float w = sqrt(r*r-h*h); // constant for any given shape
    p.x = abs(p.x);
    float s = max( (h-r)*p.x*p.x+w*w*(h+r-2.0*p.y), h*p.x-w*p.y );
    return (s<0.0) ? length(p)-r :
            (p.x<w) ? h - p.y     :
            length(p-vec2(w,h));
}

float sdUnevenCapsule( vec2 p, float r1, float r2, float h )
{
    p.x = abs(p.x);
    float b = (r1-r2)/h;
    float a = sqrt(1.0-b*b);
    float k = dot(p,vec2(-b,a));
    if( k < 0.0 ) return length(p) - r1;
    if( k > a*h ) return length(p-vec2(0.0,h)) - r2;
    return dot(p, vec2(a,b) ) - r1;
}

mat2 rotate2D(float a){
    return mat2(cos(a), sin(a), -sin(a), cos(a));
}

// https://stackoverflow.com/questions/15095909/from-rgb-to-hsv-in-opengl-glsl
// All components are in the range [0…1], including hue.
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float render(vec2 uv){
    vec2 pos = cartesianToLogPolar(uv);
    pos.x += scalingTime;
//    pos.y += rotationTime;
    pos *= float(copies)/PI;
    pos = fract(pos) - 0.5;
    pos *= rotate2D(-PI / 2.0);
    float sdCap = sdCutDisk(pos, 0.2, 0.04);
    float stemHeight = 0.20;
    float sdStem = sdUnevenCapsule(pos - vec2(0, -stemHeight), 0.04, 0.01,  stemHeight);
    float pct = smoothstep(0.3, 0., min(sdCap, sdStem));
    return pct;
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float d = length(uv);
    float n = render(uv);
    float hue = mod(d*0.5+n-scalingTime, 1.);
    float sat = clamp(d+n*0.8, 0., 1.);
    float val = 0.5+0.5 * n;
    vec3 col = hsv2rgb(vec3(hue,sat,val));
    gl_FragColor = vec4(col, 1.);
}