uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float timeRadius;
uniform float strength;
uniform float baseAngle;
uniform float angleRange;
uniform float frequency;
uniform float freqMult;
uniform float ampMult;
uniform float centerForce;
uniform int octaves;

uniform float darken;

#define TAU 6.28318530718

mat3 m1 = mat3(+0.4122214708, +0.5363325363, +0.0514459929,
+0.2119034982, +0.6806995451, +0.1073969566,
+0.0883024619, +0.2817188376, +0.6299787005);

mat3 inverse_m1 = mat3(+4.0767416621, -3.3077115913, +0.2309699292,
-1.2684380046, +2.6097574011, -0.3413193965,
-0.0041960863, -0.7034186147, +1.7076147010);

mat3 m2 = mat3(+0.2104542553, +0.7936177850, -0.0040720468,
+1.9779984951, -2.4285922050, +0.4505937099,
+0.0259040371, +0.7827717662, -0.8086757660);

mat3 inverse_m2 = mat3(1., +0.3963377774, +0.2158037573,
1., -0.1055613458, -0.0638541728,
1., -0.0894841775, -1.2914855480);

float cbrt( float x ) // https://www.shadertoy.com/view/wts3RX (needed for negative cube roots)
{
    float y = sign(x) * uintBitsToFloat( floatBitsToUint( abs(x) ) / 3u + 0x2a514067u );

    for( int i = 0; i < 1; ++i )
    y = ( 2. * y + x / ( y * y ) ) * .333333333;

    for( int i = 0; i < 1; ++i )
    {
        float y3 = y * y * y;
        y *= ( y3 + 2. * x ) / ( 2. * y3 + x );
    }

    return y;
}

vec3 cbrt( vec3 xyz )
{
    return vec3(cbrt(xyz.x), cbrt(xyz.y), cbrt(xyz.z));
}

vec3 rgb2oklab(vec3 rgb)
{
    return cbrt(rgb * m1)*m2;
}

vec3 oklab2rgb(vec3 oklab)
{
    return pow(oklab * inverse_m2, vec3(3.)) * inverse_m1;
}

vec3 oklab2oklch(vec3 oklab)
{
    return vec3(oklab.x,
    sqrt(oklab.y * oklab.y + oklab.z * oklab.z),
    atan(oklab.z, oklab.y)/TAU);
}

vec3 oklch2oklab(vec3 oklch)
{
    return vec3(oklch.x,
    oklch.y * cos(oklch.z*TAU),
    oklch.y * sin(oklch.z*TAU));
}

vec3 rgb2oklch(vec3 rgb) { return oklab2oklch(rgb2oklab(rgb)); }
vec3 oklch2rgb(vec3 oklch) { return oklab2rgb(oklch2oklab(oklch)); }

vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
float permute(float x){ return floor(mod(((x*34.0)+1.0)*x, 289.0)); }
vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }
float taylorInvSqrt(float r){ return 1.79284291400159 - 0.85373472095314 * r; }
vec4 grad4(float j, vec4 ip){
    const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
    vec4 p, s;

    p.xyz = floor(fract (vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
    p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
    s = vec4(lessThan(p, vec4(0.0)));
    p.xyz = p.xyz + (s.xyz*2.0 - 1.0) * s.www;

    return p;
}
float snoise(vec4 v){
    const vec2  C = vec2(0.138196601125010504, // (5 - sqrt(5))/20  G4
    0.309016994374947451);// (sqrt(5) - 1)/4   F4
    // First corner
    vec4 i  = floor(v + dot(v, C.yyyy));
    vec4 x0 = v -   i + dot(i, C.xxxx);

    // Other corners

    // Rank sorting originally contributed by Bill Licea-Kane, AMD (formerly ATI)
    vec4 i0;

    vec3 isX = step(x0.yzw, x0.xxx);
    vec3 isYZ = step(x0.zww, x0.yyz);
    //  i0.x = dot( isX, vec3( 1.0 ) );
    i0.x = isX.x + isX.y + isX.z;
    i0.yzw = 1.0 - isX;

    //  i0.y += dot( isYZ.xy, vec2( 1.0 ) );
    i0.y += isYZ.x + isYZ.y;
    i0.zw += 1.0 - isYZ.xy;

    i0.z += isYZ.z;
    i0.w += 1.0 - isYZ.z;

    // i0 now contains the unique values 0,1,2,3 in each channel
    vec4 i3 = clamp(i0, 0.0, 1.0);
    vec4 i2 = clamp(i0-1.0, 0.0, 1.0);
    vec4 i1 = clamp(i0-2.0, 0.0, 1.0);

    //  x0 = x0 - 0.0 + 0.0 * C
    vec4 x1 = x0 - i1 + 1.0 * C.xxxx;
    vec4 x2 = x0 - i2 + 2.0 * C.xxxx;
    vec4 x3 = x0 - i3 + 3.0 * C.xxxx;
    vec4 x4 = x0 - 1.0 + 4.0 * C.xxxx;

    // Permutations
    i = mod(i, 289.0);
    float j0 = permute(permute(permute(permute(i.w) + i.z) + i.y) + i.x);
    vec4 j1 = permute(permute(permute(permute (
    i.w + vec4(i1.w, i2.w, i3.w, 1.0))
    + i.z + vec4(i1.z, i2.z, i3.z, 1.0))
    + i.y + vec4(i1.y, i2.y, i3.y, 1.0))
    + i.x + vec4(i1.x, i2.x, i3.x, 1.0));
    // Gradients
    // ( 7*7*6 points uniformly over a cube, mapped onto a 4-octahedron.)
    // 7*7*6 = 294, which is close to the ring size 17*17 = 289.

    vec4 ip = vec4(1.0/294.0, 1.0/49.0, 1.0/7.0, 0.0);

    vec4 p0 = grad4(j0, ip);
    vec4 p1 = grad4(j1.x, ip);
    vec4 p2 = grad4(j1.y, ip);
    vec4 p3 = grad4(j1.z, ip);
    vec4 p4 = grad4(j1.w, ip);

    // Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    p4 *= taylorInvSqrt(dot(p4, p4));

    // Mix contributions from the five corners
    vec3 m0 = max(0.6 - vec3(dot(x0, x0), dot(x1, x1), dot(x2, x2)), 0.0);
    vec2 m1 = max(0.6 - vec2(dot(x3, x3), dot(x4, x4)), 0.0);
    m0 = m0 * m0;
    m1 = m1 * m1;
    return 49.0 * (dot(m0*m0, vec3(dot(p0, x0), dot(p1, x1), dot(p2, x2)))
    + dot(m1*m1, vec2(dot(p3, x3), dot(p4, x4))));
}

float fbm (vec4 p, float amp, float freq) {
    float sum = 0.;
    // Loop of octaves
    for (int i = 0; i < octaves; i++) {
        sum += amp*snoise(p*freq);
        freq *= freqMult;
        amp *= ampMult;
        p += vec4(3.123, 2.456, 1.121, 2.4545);
    }
    return sum;
}

float noise(vec2 p, vec2 t, float amp, float freq){
    return amp*snoise(vec4(p*freq, t));
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 uvColor = texture2D(texture, uv).rgb;
    vec2 wheelOfTime = vec2(timeRadius*cos(time), timeRadius*sin(time));
    float noiseAngle = baseAngle + fbm(vec4(uv, wheelOfTime), angleRange, frequency);
    vec2 dir = vec2(cos(noiseAngle), sin(noiseAngle));
    float fromCenterAngle = atan(uv.y - 0.5, uv.x - 0.5);
    vec2 fromCenterDir = vec2(centerForce*cos(fromCenterAngle), centerForce*sin(fromCenterAngle));
    dir += fromCenterDir;
    vec2 neighbourUV = (gl_FragCoord.xy + dir.xy) / resolution.xy;
    float stepsz = 0.05;
    vec2 potato = vec2(
        texture2D(texture, neighbourUV + vec2(1,0) * stepsz).r -
        texture2D(texture, neighbourUV + vec2(-1,0) * stepsz).r,
        texture2D(texture, neighbourUV + vec2(0,1) * stepsz).r -
        texture2D(texture, neighbourUV + vec2(0,-1) * stepsz).r
    );

    neighbourUV += dir*potato*0.004*noiseAngle*0.05;
    if(mod(time, 2) < 0.01){
        uvColor = oklch2rgb(uvColor);
    }
    uvColor = mix(oklch2rgb(vec3(length(uvColor),0.6,uvColor.x)),uvColor, length(uvColor));

    vec3 neighbourColor = texture2D(texture, neighbourUV).rgb;
    vec3 clrMix = mix(uvColor, neighbourColor, strength);

    clrMix -= darken;

    gl_FragColor = vec4(clrMix, 1.);
}