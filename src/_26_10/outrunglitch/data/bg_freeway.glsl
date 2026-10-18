uniform vec2 resolution;
uniform float loopPhase;
uniform float loopFrame;

const float TAU = 6.2831853;
uniform float roadScroll;
uniform float cityScroll;
uniform float cityParallax;
uniform float boost;
uniform float streaks;
uniform float breath;
uniform float breathDepth;
uniform float hueDrift;
uniform vec3 colA;
uniform vec3 colB;
uniform vec3 colC;
uniform float roadY;
uniform float skyDim;

float hash(float n) { return fract(sin(n * 12.9898) * 43758.5453); }
float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

vec3 hueRot(vec3 c, float a) {
    vec3 k = vec3(0.57735);
    return c * cos(a) + cross(k, c) * sin(a) + k * dot(k, c) * (1.0 - cos(a));
}

vec3 breathingGradient(vec2 uv, float aspect) {
    vec2 q = vec2(uv.x * aspect, uv.y);
    vec2 p1 = vec2(0.25 + 0.15 * sin(TAU * loopPhase), 0.75 + 0.10 * cos(TAU * loopPhase));
    vec2 p2 = vec2(0.75 + 0.15 * cos(TAU * loopPhase), 0.55 + 0.15 * sin(TAU * loopPhase * 2.0));
    vec2 p3 = vec2(0.50 + 0.20 * sin(TAU * loopPhase), 0.20 + 0.10 * cos(TAU * loopPhase * 2.0));
    float k = 3.0 + 2.0 * breath * breathDepth;
    float w1 = exp(-pow(length(q - vec2(p1.x * aspect, p1.y)), 2.0) * k);
    float w2 = exp(-pow(length(q - vec2(p2.x * aspect, p2.y)), 2.0) * k);
    float w3 = exp(-pow(length(q - vec2(p3.x * aspect, p3.y)), 2.0) * k);
    vec3 c = (colA * w1 + colB * w2 + colC * w3) / (w1 + w2 + w3 + 0.0001);
    c *= 1.3 * (1.0 - 0.4 * breathDepth + breathDepth * breath);
    return mix(vec3(dot(c, vec3(0.333))), c, 1.45);
}

// one parallax layer of building silhouettes; returns mask in x, lit window in y
vec2 skyline(vec2 uv, float aspect, float scroll, float cells, float baseH, float ampH, float seed) {
    float xw = uv.x * aspect + cityScroll * scroll * cityParallax;
    float cx = xw * cells;
    float id = floor(cx);
    float h = baseH + ampH * hash(id + seed);
    float inside = step(uv.y, h);
    float wx = fract(cx);
    float winCol = floor(wx * 4.0);
    float winRow = floor(uv.y * 70.0);
    float cellMask = step(0.2, fract(wx * 4.0)) * step(0.3, fract(uv.y * 70.0));
    float lit = step(0.72, hash2(vec2(winCol + id * 5.0, winRow + seed))) * cellMask * inside * step(uv.y, h - 0.01);
    return vec2(inside, lit);
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float aspect = resolution.x / resolution.y;
    float asphaltTop = roadY + 0.10;
    float barrierTop = asphaltTop + 0.035;

    vec3 grad = breathingGradient(uv, aspect);
    vec3 col = grad * skyDim;

    // glow behind the skyline
    col += mix(colA, colB, uv.x) * exp(-abs(uv.y - barrierTop - 0.12) * 6.0) * 0.45;

    // far skyline
    vec2 far = skyline(uv, aspect, 0.08, 6.0, barrierTop + 0.05, 0.22, 3.0);
    col = mix(col, grad * 0.3 + vec3(0.02, 0.0, 0.05), far.x);
    col += vec3(1.0, 0.8, 0.5) * far.y * 0.5;

    // near skyline
    vec2 near = skyline(uv, aspect, 0.25, 3.0, barrierTop + 0.02, 0.12, 11.0);
    col = mix(col, grad * 0.07 + vec3(0.01, 0.0, 0.03), near.x);
    col += vec3(1.0, 0.3, 0.7) * near.y * 0.9;

    // street lamps
    float lampPeriod = 1.5;
    float lx = mod(uv.x * aspect + roadScroll * 0.6, lampPeriod) - lampPeriod * 0.5;
    float lampH = barrierTop + 0.22;
    float pole = step(abs(lx), 0.004) * step(barrierTop, uv.y) * step(uv.y, lampH);
    float arm = step(abs(uv.y - lampH), 0.004) * step(0.0, lx + 0.12) * step(lx, 0.004);
    vec2 lampPos = vec2(-0.12, lampH - 0.012);
    float lampD = length(vec2(lx, uv.y) - lampPos);
    col = mix(col, vec3(0.02, 0.0, 0.05), max(pole, arm));
    col += vec3(1.0, 0.85, 0.6) * exp(-lampD * 20.0) * 1.1;
    col += vec3(1.0, 0.9, 0.7) * step(lampD, 0.008);

    // barrier with moving posts
    if (uv.y > asphaltTop && uv.y < barrierTop) {
        float post = step(0.5, fract((uv.x * aspect + roadScroll * 1.2) * 4.0));
        col = mix(vec3(0.08, 0.05, 0.12), vec3(0.2, 0.12, 0.28), post);
        col += colB * 0.25 * step(barrierTop - 0.006, uv.y);
    }

    // asphalt
    if (uv.y <= asphaltTop) {
        float v = uv.y / asphaltTop;
        col = mix(grad * 0.03 + vec3(0.01, 0.0, 0.03), grad * 0.28, v);

        // warm pool of light under each street lamp
        col += vec3(1.0, 0.75, 0.45) * exp(-pow((lx + 0.12) * 3.0, 2.0)) * (0.3 + 0.7 * v) * 0.45;

        // speed streaks
        float band = floor(uv.y * 90.0);
        float hb = hash(band);
        float len = 0.15 + 0.5 * hb;
        float sx = fract((uv.x * aspect) * 0.4 + roadScroll * (1.2 + hb * 2.0) * 0.4 + hb * 7.0);
        float streak = step(0.8, hash(band + 17.0)) * smoothstep(len, len - 0.1, sx) * step(0.0, sx);
        col += colC * streak * 0.35;

        // lane dashes
        float dashY = roadY - 0.12;
        float dash = step(abs(uv.y - dashY), 0.005) * step(0.5, fract((uv.x * aspect + roadScroll * 1.6) * 1.6));
        float dashOn = step(0.5, fract((uv.x * aspect + roadScroll * 1.6) * 1.6));
        col += colB * exp(-abs(uv.y - dashY) * 90.0) * dashOn * 0.35;
        col = mix(col, vec3(1.0, 0.9, 0.95), dash * 0.85);

        // edge line
        float edge = step(abs(uv.y - (asphaltTop - 0.012)), 0.003);
        col += colB * exp(-abs(uv.y - (asphaltTop - 0.012)) * 60.0) * 0.5;
        col = mix(col, colB * 1.4, edge * 0.9);
    }

    // foreground speed streaks and sparks, stronger while the car accelerates
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float rows = 38.0 + fi * 27.0;
        float band = floor(uv.y * rows);
        float hb = hash(band + fi * 53.0);
        float lane = step(0.82 - 0.1 * boost, hb);
        float len = (0.25 + 0.6 * hb) * (1.0 + boost * 2.0);
        float sx = fract(uv.x * aspect * 0.35 + roadScroll * (0.9 + fi * 0.7 + hb * 1.2) * 0.35 + hb * 9.0);
        float body = smoothstep(len * 0.5, 0.0, sx) * smoothstep(0.0, 0.02, sx);
        float thin = smoothstep(0.35, 0.0, abs(fract(uv.y * rows) - 0.5));
        float a = lane * body * thin * streaks * (0.25 + 0.75 * boost);
        col += mix(vec3(1.0), colB, 0.5) * a * (0.9 - fi * 0.2);
    }
    float sparkRows = 160.0;
    float sBand = floor(uv.y * sparkRows);
    float sh = hash(sBand + 101.0);
    float sparkX = fract(uv.x * aspect * 0.5 + roadScroll * (1.6 + sh * 2.0) * 0.5 + sh * 5.0);
    float spark = step(0.9, hash(sBand + 7.0)) * smoothstep(0.012 + boost * 0.05, 0.0, sparkX) * step(abs(fract(uv.y * sparkRows) - 0.5), 0.3);
    col += vec3(1.0, 0.9, 0.7) * spark * streaks * (0.5 + boost);

    col = hueRot(col, hueDrift * sin(TAU * loopPhase));
    col += (hash2(gl_FragCoord.xy + loopFrame) - 0.5) * 0.04;
    gl_FragColor = vec4(col, 1.0);
}
