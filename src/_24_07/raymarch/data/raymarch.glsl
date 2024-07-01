// to research: https://www.shadertoy.com/view/WsV3zz

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

const float MAX_STEPS = 120;
const float MAX_DIST = 100.;
const float SURFACE_DIST = 0.001;

float intersectSDF(float distA, float distB) {
    return max(distA, distB);
}

float unionSDF(float distA, float distB) {
    return min(distA, distB);
}

float differenceSDF(float distA, float distB) {
    return max(distA, -distB);
}

float sdOctahedron( vec3 p, float s )
{
    p = abs(p);
    float m = p.x+p.y+p.z-s;
    vec3 q;
    if( 3.0*p.x < m ) q = p.xyz;
    else if( 3.0*p.y < m ) q = p.yzx;
    else if( 3.0*p.z < m ) q = p.zxy;
    else return m*0.57735027;

    float k = clamp(0.5*(q.z-q.y+s),0.0,s);
    return length(vec3(q.x,q.y-s+k,q.z-k));
}

float sdPlane(vec3 p, float y){
    return abs(p.y - y);
}

float sdSphere(vec3 p, float r){
    return length(p) - r;
}

float sdSine(vec3 p) {
    return 1.0 - (sin(p.x) + sin(p.y) + sin(p.z))/3.0;
}

mat2 rotate2d(float a){
    return mat2(cos(a), -sin(a), sin(a), cos(a));
}

mat4 rotation3d(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(
    oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
    0.0,                                0.0,                                0.0,                                1.0
    );
}

// Tweaked Cosine color palette function from Inigo Quilez
vec3 getColor(float amount) {
    vec3 color = vec3(0.3, 0.5, 0.9) +vec3(0.9, 0.4, 0.2) * cos(6.2831 * (vec3(0.30, 0.20, 0.20) + amount * vec3(1.0)));
    return color * amount;
}

float scene(vec3 p){
//    vec3 prep = mod(p+0.5, 1.)-0.5;
    float t = time * 0.5;
//    p.xy *= rotate2d(-0.1);
//    p.xz *= rotate2d(t);
    float sphere = sdSphere(p-vec3(0,0,1.5), 0.35);
    float sine = 0.5 - sdSine(30.*p);
    return intersectSDF(sine, sphere);
}

float smin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b-a)/k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}


vec3 getNormal(vec3 p) {
    vec2 e = vec2(.01, 0);
    vec3 n = scene(p) - vec3(
        scene(p-e.xyy),
        scene(p-e.yxy),
        scene(p-e.yyx));
    return normalize(n);
}

float softShadows(vec3 ro, vec3 rd, float mint, float maxt, float k ) {
    float resultingShadowColor = 1.0;
    float t = mint;
    for(int i = 0; i < 50 && t < maxt; i++) {
        float h = scene(ro + rd*t);
        if( h < 0.001 )
        return 0.0;
        resultingShadowColor = min(resultingShadowColor, k*h/t );
        t += h;
    }
    return resultingShadowColor ;
}

float rayMarch(vec3 rayOrigin, vec3 rayDirection){
    float distanceFromOrigin = 0.;
    for(int i = 0; i < MAX_STEPS; i++)
    {
        vec3 p = rayOrigin + distanceFromOrigin * rayDirection;
        float distanceToScene = scene(p);
        distanceFromOrigin += distanceToScene;
        bool foundSurface = distanceToScene < SURFACE_DIST;
        bool exceededMax = distanceFromOrigin > MAX_DIST;
        if (foundSurface || exceededMax){
            break;
        }
    }
    return distanceFromOrigin;
}

vec3 render(vec2 uv){
    vec3 col = vec3(0);
    float t = time;
    vec3 rayOrigin = vec3(0., 0., 0.);
    vec3 rayDirection = normalize(vec3(uv.x, uv.y, 1.));
    float distanceFromOrigin = rayMarch(rayOrigin, rayDirection);
    vec3 p = rayOrigin + distanceFromOrigin * rayDirection;
    vec3 lightPosition = vec3(-10.0 * cos(t), 10.0 * sin(t), -10.0 * abs(sin(-t * 0.5)));

    vec3 lightDir = normalize(lightPosition - p);
    vec3 normal = getNormal(p);
    float light = dot(normal, lightDir);
    if(distanceFromOrigin < MAX_DIST){

        float diffuse = max(dot(normal, lightDir), 0.0);
        float shadows = softShadows(p, lightDir, 0.1, 5.0, 64.0);
        col = getColor(diffuse) * shadows;
    }else{
        col = vec3(0.0);
    }

    return col;
}

vec3 aa(vec2 uv){
    vec3 col = vec3(0);
    vec2 n = vec2(dFdxFine(uv.x), dFdxFine(uv.y));
    return (
    render(uv) +
    (render(uv + vec2(+n.x, 0.)))   * 0.20 +
    (render(uv + vec2(-n.x, 0.)))   * 0.20 +
    (render(uv + vec2(0., -n.y)))   * 0.20 +
    (render(uv + vec2(0., +n.y)))   * 0.20 +
    (render(uv + vec2(+n.x, +n.y))) * 0.05 +
    (render(uv + vec2(+n.x, -n.y))) * 0.05 +
    (render(uv + vec2(-n.x, -n.y))) * 0.05 +
    (render(uv + vec2(-n.x, +n.y))) * 0.05
    ) / 2.;
}

void main(){
    vec2 uv = (gl_FragCoord.xy - .5*resolution) / resolution.y;
    //    vec3 col = aa(uv);
    vec3 col = render(uv);
    gl_FragColor = vec4(col, 1.);
}