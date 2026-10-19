uniform vec2 resolution;

uniform vec3 eye;
uniform vec3 camFwd;
uniform vec3 camRight;
uniform vec3 camDown;
uniform float tanHalf;

uniform vec3 feltColor;
uniform float spotStrength;
uniform float spotRadius;
uniform vec2 spotPos;
uniform float fogDist;
uniform float grain;
uniform float grainScale;
uniform float grainFade;
uniform float nap;
uniform float ambientFloor;

uniform float carYaw;
uniform vec2 carHalf;

uniform float headWash;
uniform float headLen;
uniform float headWidth;
uniform float tailWash;

uniform vec3 neonA;
uniform vec3 neonB;
uniform float underglow;
uniform float underSize;
uniform float underCore;
uniform float haze;
uniform float neonPool;

float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash2(i), hash2(i + vec2(1.0, 0.0)), f.x),
               mix(hash2(i + vec2(0.0, 1.0)), hash2(i + vec2(1.0, 1.0)), f.x), f.y);
}

float boxDist(vec2 p, vec2 b) {
    vec2 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0);
}

vec2 toCar(vec2 p) {
    float a = carYaw;
    return vec2(p.x * cos(a) - p.y * sin(a), p.x * sin(a) + p.y * cos(a));
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float aspect = resolution.x / resolution.y;
    vec2 s = vec2(uv.x - 0.5, 0.5 - uv.y) * 2.0;
    vec3 dir = normalize(camFwd + s.x * tanHalf * aspect * camRight + s.y * tanHalf * camDown);

    vec3 deep = feltColor * 0.06;
    vec3 col = deep;
    if (dir.y > 0.0005) {
        float dist = -eye.y / dir.y;
        vec2 p = eye.xz + dir.xz * dist;

        // felt: fine fibre speckle that softens with distance, like depth of field
        float fine = hash2(floor(p * grainScale)) - 0.5;
        float soft = vnoise(p * grainScale * 0.25) - 0.5;
        float focus = exp(-dist * grainFade);
        float fibre = mix(soft, fine, focus) * grain;
        float napShade = (vnoise(p * 1.7) - 0.5) * nap;
        vec3 base = feltColor * (1.0 + fibre + napShade);

        // the pool of light the car sits in
        vec2 sp = p - spotPos;
        float pool = exp(-dot(sp, sp) / (spotRadius * spotRadius));
        col = base * (ambientFloor + spotStrength * pool);

        // light thrown on the felt: warm ahead of the headlights, red behind the tail lights
        vec2 cp = toCar(p);
        float ahead = max(cp.y - 1.95, 0.0);
        float spread = headWidth * (1.0 + ahead * 1.5);
        float lampL = exp(-pow((cp.x - 0.58) / spread, 2.0)) + exp(-pow((cp.x + 0.58) / spread, 2.0));
        col += vec3(1.0, 0.88, 0.65) * lampL * exp(-ahead / headLen) * smoothstep(-0.05, 0.25, cp.y - 1.95) * headWash * 0.5;
        float behind = max(-cp.y - 1.95, 0.0);
        float tspread = headWidth * (1.0 + behind);
        float tailL = exp(-pow((cp.x - 0.65) / tspread, 2.0)) + exp(-pow((cp.x + 0.65) / tspread, 2.0));
        col += vec3(1.0, 0.1, 0.08) * tailL * exp(-behind / (headLen * 0.5)) * smoothstep(-0.05, 0.2, -cp.y - 1.95) * tailWash * 0.5;

        // coloured pools off to both sides and the glow of the underbody neon
        vec2 pa = p - vec2(-3.5, -2.5);
        vec2 pb = p - vec2(3.5, -2.5);
        col += neonA * exp(-dot(pa, pa) / 9.0) * neonPool * 0.6;
        col += neonB * exp(-dot(pb, pb) / 9.0) * neonPool * 0.6;
        // brightest right at the body and dropping off quickly with distance, with a long soft tail
        // an oval around the car, not a rectangle
        vec2 ovalHalf = carHalf * 0.8;
        float ud = max(length(toCar(p) / ovalHalf) - 1.0, 0.0) * ovalHalf.x;
        float under = 0.6 * exp(-ud / underSize) + underCore * exp(-ud / (underSize * 0.2));
        col += neonB * under * underglow * 0.7;

        col = mix(col, deep, 1.0 - exp(-dist / fogDist));
    }
    // haze: a coloured glow hugging the horizon
    col += mix(neonA, neonB, smoothstep(-0.8, 0.8, dir.x)) * haze * 0.35 * exp(-abs(dir.y) * 7.0);
    gl_FragColor = vec4(col, 1.0);
}
