uniform vec2 resolution;
uniform float time;
uniform sampler2D tex0;

// https://bottosson.github.io/posts/oklab/

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
void main(){

    vec2 uv = gl_FragCoord.xy/resolution.xy;
    gl_FragColor = texture(tex0, uv);
    vec2 d = vec2(
        dFdx(gl_FragColor.x),
        dFdy(gl_FragColor.y)
    );
    float h = gl_FragColor.x + gl_FragColor.y + gl_FragColor.z;
    gl_FragColor.xyz = oklch2rgb(
        vec3(
            length(gl_FragColor.xyz),
            0.1 * length(gl_FragColor.xyz) + length(d)*0.,
            1 + h * 5. + length(d)*1.0 + time
        )
    );
    //gl_FragColor.x = 1.0;
}