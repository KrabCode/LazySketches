// to research: https://www.shadertoy.com/view/WsV3zz

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

const float MAX_DIST = 80.;
const float MAX_STEPS = 300;
const float SURFACE_DIST = 0.001;

float mapPlane(vec3 p, float y){
    return abs(p.y - y);
}

float mapSphere(vec3 p, vec3 pos, float r){
    return length(p-pos) - r;
}

float map(vec3 p){
    return mapSphere(mod(p+0.5, 1.)-0.5, vec3(0), 0.075);
}

vec3 getNormal(vec3 p){
    const vec2 epsilon = vec2(.0001,0);
    float d0 = map(p);
    vec3 d1 = vec3(
        map(p-epsilon.xyy),
        map(p-epsilon.yxy),
        map(p-epsilon.yyx));
    return normalize(d0 - d1);
}

float rayMarch(vec3 rayOrigin, vec3 rayDirection){
    float distanceFromOrigin = 0.;
    for(int i = 0; i < MAX_STEPS; i++)
    {
        vec3 p = rayOrigin + distanceFromOrigin * rayDirection;
        float distanceToScene = map(p);
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
    vec3 rayOrigin = vec3(0.5+cos(t), 0.5+sin(t), t);
    vec3 rayDirection = normalize(vec3(uv.x, uv.y, 1.));
    vec3 lightDir = normalize(vec3(0.0, -0.2, -.5));
    float distanceFromOrigin = rayMarch(rayOrigin, rayDirection);
    vec3 p = rayOrigin + distanceFromOrigin * rayDirection;
    vec3 normal = getNormal(p);
    float light = dot(normal, lightDir);
    if(distanceFromOrigin < MAX_DIST){
        col = vec3(light);
    }else{
        col = vec3(0.1);
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
    vec3 aaCol = aa(uv);
    gl_FragColor = vec4(aaCol, 1.);
}