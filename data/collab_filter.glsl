
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform vec2 ball_pos;
uniform int frame;

float[9] blur_kern = float[9](
    0.7, 1., 0.7,
    1., 1., 1.,
    0.7, 1., 0.7
);

float[9] sharpen_kern = float[9](
    -0.5, -2., -0.5,
    -2., 10., -2.,
    -0.5, -2., -0.5
);

vec4 apply_kern(vec2 uv, float[9] kern){
    vec4 C = vec4(0);
    float kern_sum = 0;
    for(int y = 1; y >= -1; y--){
        for(int x = 1; x >= -1; x--){
            int idx = (y+1)*3 + x + 1 ;
            float kern_val = kern[idx];
            vec2 uv_step = 1./vec2(resolution.xy);
            vec2 ioffs = vec2(x,y);
            vec2 samp_uv = uv + uv_step * ioffs*4.5*sin(time/4.0);
            vec4 samp = texture(texture, samp_uv);
            C += samp*kern_val;
            kern_sum += kern_val;
        }
    }
    C /= kern_sum;
    return C;
}
#define rot(a) mat2(cos(a),-sin(a),sin(a),cos(a))

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec4 col = texture(texture, uv).rgba;
    vec2 deriv = vec2(dFdx(col.x), dFdy(col.x));
    deriv = vec2(
    texture(texture, uv + vec2(1,0)/resolution).x -
    texture(texture, uv - vec2(1,0)/resolution).x,
    texture(texture, uv + vec2(0,1)/resolution).x -
    texture(texture, uv - vec2(0,1)/resolution).x);

    vec2 balloffs = vec2(ball_pos.x/resolution.x, 1.-ball_pos.y/resolution.y);
    //uv -= 0.5;
    uv -= balloffs;
    uv *= 0.994 - fract(time/60*135.0)*0.007;

    uv += deriv*0.025*sin(time)*0.3 * dot(uv,uv)*3.;
    uv *= rot(0.002);
    uv += balloffs;
    //uv += 0.5;
    if(frame % 2 == 0){
        col = apply_kern(uv, blur_kern);
    } else {
        col = apply_kern(uv, sharpen_kern);
    }
    if(frame == 0
         //|| true
    ){
        //vec2 uv = gl_FragCoord.xy / resolution.xy;
        col = sin(cross(uv.xyx*56., uv.yxy*2.)).xyzz;
    }
    col = clamp(col,0.,1.0);
    col.a = 1;
    gl_FragColor = col;
}