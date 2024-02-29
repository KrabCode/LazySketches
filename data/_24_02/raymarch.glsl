// to research: https://www.shadertoy.com/view/WsV3zz

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

const int MAX_STEPS = 10;
const float MAX_STEP_SIZE = 0.1;
const float epsilon = 0.001;

float mapSphere(vec3 p, vec3 pos, float r){
    return length(p-pos) - r;
}

float map(vec3 p){
    return mapSphere(p, vec3(0, 1, 1), 0.1);
}

vec3 getNormal(vec3 p){
    vec2 e = vec2(0.1, 0);
    return normalize(vec3(
    map(p+e.xyy) - map(p-e.xyy),
    map(p+e.yxy) - map(p-e.yxy),
    map(p+e.yyx) - map(p-e.yyx)
    ));
}

vec3 render(vec2 uv){
    vec3 col = vec3(0);
    vec3 rayOrigin = vec3(0, 1, 0);
    vec3 rayDir = normalize(vec3(uv.x, uv.y, 1.));
    float t = time;
    vec3 lightDir = normalize(vec3(cos(t), sin(t), -1.));
    float stepSize = MAX_STEP_SIZE;
    vec3 p = rayOrigin;
    bool hit = false;
    float nearMissDistance = 1000.;
    vec3 nearestMissPoint = rayOrigin;
    for (int step = 0; step < MAX_STEPS; step++){
        p += rayDir * stepSize;
        float d = map(p);
        stepSize = d*1.0;
        if (d < epsilon){
            vec3 normalDir = getNormal(p);
            col = vec3(dot(normalDir, lightDir));
            hit = true;
            break;
        }
        if (d < nearMissDistance){
            nearMissDistance = min(nearMissDistance, d);
            nearestMissPoint = p;
        }
    }
    if (!hit){
        vec3 normalDir = getNormal(nearestMissPoint);
        col = vec3(dot(normalDir, lightDir));
    }
    return col;
}

vec3 aa(vec2 uv){
    vec3 col = vec3(0);
    vec2 offset = vec2(1./resolution.x, 1./resolution.y);
    return (render(uv + vec2(-offset.x, -offset.y)) +
            render(uv + vec2(offset.x, -offset.y)) +
            render(uv + vec2(-offset.x, offset.y)) +
            render(uv + vec2(offset.x, offset.y))) / 4.0;
}

void main(){
    vec2 uv = (gl_FragCoord.xy - .5*resolution) / resolution.y;
    vec3 aaCol = aa(uv);
    gl_FragColor = vec4(aaCol, 1.);
}