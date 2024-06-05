
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform int frame;

float[9] blur_kern = float[9](
    0.5, 1., 0.5,
    1., 1., 1.,
    0.5, 1., 0.5
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
            vec2 samp_uv = uv + uv_step * ioffs*1.0;
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

    uv -= 0.5;
    uv *= 0.9992;
    uv *= rot(0.005);
    uv += 0.5;
    vec2 deriv = vec2(dFdx(col.x), dFdy(col.x));
    uv -= deriv*0.05;
    if(frame % 2 == 0){
        col = apply_kern(uv, blur_kern);
    } else {
        col = apply_kern(uv, sharpen_kern);
    }
    if(frame == 0
        //|| true
    ){
        col = sin(cross(uv.xyx*27., uv.yxy*42.)).xyzz;
    }
    col = clamp(col,0.,1.0);
    col.a = 1;
    gl_FragColor = col;
}