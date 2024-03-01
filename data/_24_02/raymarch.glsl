// to research: https://www.shadertoy.com/view/WsV3zz

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

const float MAX_DIST = 80.;
const float MAX_STEPS = 300;
const float SURFACE_DIST = 0.0001;

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

float sdSphere(vec3 p, vec3 pos, float r){
    return length(p-pos) - r;
}

float sd(vec3 p){
    vec3 prep = mod(p+0.5, 1.)-0.5;
//    return sdSphere(prep, vec3(0), 0.075);
    return sdOctahedron(prep, 0.25);
}

vec3 getNormal(vec3 p){
    const vec2 epsilon = vec2(.0001,0);
    float d0 = sd(p);
    vec3 d1 = vec3(
        sd(p-epsilon.xyy),
        sd(p-epsilon.yxy),
        sd(p-epsilon.yyx));
    return normalize(d0 - d1);
}

float rayMarch(vec3 rayOrigin, vec3 rayDirection){
    float distanceFromOrigin = 0.;
    for(int i = 0; i < MAX_STEPS; i++)
    {
        vec3 p = rayOrigin + distanceFromOrigin * rayDirection;
        float distanceToScene = sd(p);
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
    vec3 rayOrigin = vec3(0.5, 0.5, t);
    vec3 rayDirection = normalize(vec3(uv.x, uv.y, 1.));
    vec3 lightDir = normalize(vec3(0.7, 0.5, -1.));
    float distanceFromOrigin = rayMarch(rayOrigin, rayDirection);
    vec3 p = rayOrigin + distanceFromOrigin * rayDirection;
    vec3 normal = getNormal(p);
    float light = dot(normal, lightDir);
    if(distanceFromOrigin < MAX_DIST){
        col = vec3(light);
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