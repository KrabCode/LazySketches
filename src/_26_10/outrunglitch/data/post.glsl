uniform sampler2D texture;
uniform vec2 resolution;
uniform float loopPhase;
uniform float loopFrame;

const float TAU = 6.2831853;

uniform float wobble;
uniform float chroma;
uniform float burstChroma;
uniform float scanlines;
uniform float vignette;
uniform float grain;

uniform float glitchAmount;
uniform float glitchRate;
uniform float tear;
uniform float lineJitter;
uniform float tracking;
uniform float ghost;

uniform float vhsBleed;
uniform float vhsHeadSwitch;
uniform float vhsDropouts;
uniform float vhsNoise;
uniform float vhsWarp;

float hash(float n) { return fract(sin(n * 12.9898) * 43758.5453); }
float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
// smooth in y so neighbouring rows move together instead of leaving isolated bright or dark lines
float stripSlide(float strip, float tick) {
    return (hash2(vec2(strip, tick)) - 0.5) * step(0.72, hash2(vec2(strip + 5.0, tick)));
}
float rowNoise(float y, float seed) {
    float i = floor(y);
    return mix(hash2(vec2(i, seed)), hash2(vec2(i + 1.0, seed)), smoothstep(0.0, 1.0, fract(y)));
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float px = resolution.y / 720.0 / resolution.x; // one pixel at 720p, scales with the frame

    // glitch bursts: random moments with a short smooth envelope
    // the burst pattern repeats every 6 seconds, same as before
    float cycle = fract(loopPhase);
    float tick = floor(cycle * glitchRate);
    float within = fract(cycle * glitchRate);
    float fires = step(0.45, hash(tick * 3.7));
    float env = fires * smoothstep(0.0, 0.1, within) * smoothstep(1.0, 0.4, within) * glitchAmount;

    uv.x += sin(uv.y * 30.0 + TAU * loopPhase * 2.0) * wobble;

    // thin horizontal strips that slide sideways, never blocks
    // the shift eases across strip edges so no pair of neighbouring pixels sees a hard uv jump
    float stripRows = 70.0;
    float stripY = uv.y * stripRows + hash(tick) * 10.0;
    float strip = floor(stripY);
    float edge = 3.0 * stripRows / resolution.y;
    float lower = smoothstep(edge, 0.0, fract(stripY));
    float upper = smoothstep(1.0 - edge, 1.0, fract(stripY));
    float slide = stripSlide(strip, tick);
    slide = mix(slide, 0.5 * (slide + stripSlide(strip - 1.0, tick)), lower);
    slide = mix(slide, 0.5 * (slide + stripSlide(strip + 1.0, tick)), upper);
    uv.x += slide * tear * env;

    // rows wobble sideways in soft waves during a burst
    float lineId = floor(gl_FragCoord.y);
    uv.x += (rowNoise(lineId * 0.25, loopFrame) - 0.5) * 2.0 * lineJitter * env;

    // a rolling tracking band, like a worn tape: the picture bends smoothly inside it
    float bandPos = fract(loopPhase);
    float bandDist = abs(uv.y - bandPos);
    float band = smoothstep(0.06, 0.0, bandDist) * tracking * (0.3 + env);
    uv.x += (rowNoise(lineId * 0.06, floor(loopFrame * 0.5)) - 0.5) * 0.05 * band;

    // occasional vertical hold slip
    uv.y += sin(uv.y * 3.0 + TAU * loopPhase * 40.0) * 0.004 * env * step(0.8, hash(tick * 9.1));

    // head-switching noise: the bottom of the frame smears sideways like a worn tape
    float headZone = smoothstep(0.07, 0.0, gl_FragCoord.y / resolution.y);
    uv.x += (hash2(vec2(floor(gl_FragCoord.y * 0.5), loopFrame)) - 0.5 + 0.6 * sin(gl_FragCoord.y * 0.4 + loopFrame * 0.3)) * 0.06 * headZone * vhsHeadSwitch;
    // slow tape warp, strongest near the top
    uv.x += sin(uv.y * 5.0 + loopPhase * TAU * 3.0) * 0.0015 * vhsWarp * (0.3 + uv.y);

    // chromatic aberration: eight taps across the spectrum, radial like a lens,
    // so the colour fringes smear smoothly instead of showing three separate copies
    vec2 dir = uv - 0.5;
    dir *= 2.0 * length(dir); // quadratic: sharp in the middle, fringed at the edges
    float aberration = chroma * (1.0 + burstChroma * (2.5 * env + band));
    vec3 acc = vec3(0.0);
    vec3 wsum = vec3(0.0);
    for (int i = 0; i < 8; i++) {
        float t = float(i) / 7.0;
        float k = t * 2.0 - 1.0;
        vec3 w = vec3(smoothstep(0.3, 1.0, t), 1.0 - abs(k), smoothstep(0.7, 0.0, t));
        acc += texture2D(texture, uv + dir * aberration * k).rgb * w;
        wsum += w;
    }
    vec3 col = acc / wsum;

    // colour bleed: chroma smears sideways while luma stays sharp, like composite video
    if (vhsBleed > 0.0) {
        vec3 yiqSum = vec3(0.0);
        for (int i = 0; i < 8; i++) {
            float o = (float(i) / 7.0 - 0.3) * vhsBleed * 24.0 * px;
            vec3 t = texture2D(texture, uv + vec2(o, 0.0)).rgb;
            yiqSum += vec3(dot(t, vec3(0.299, 0.587, 0.114)), dot(t, vec3(0.596, -0.274, -0.322)), dot(t, vec3(0.211, -0.523, 0.312)));
        }
        yiqSum /= 8.0;
        float y = dot(col, vec3(0.299, 0.587, 0.114));
        float chromaMix = clamp(vhsBleed, 0.0, 1.0);
        vec2 iq = mix(vec2(dot(col, vec3(0.596, -0.274, -0.322)), dot(col, vec3(0.211, -0.523, 0.312))), yiqSum.yz, chromaMix);
        col = mix(col, vec3(y + 0.956 * iq.x + 0.621 * iq.y, y - 0.272 * iq.x - 0.647 * iq.y, y - 1.106 * iq.x + 1.703 * iq.y), chromaMix);
    }

    // horizontal ghost echo
    vec3 echo = texture2D(texture, uv - vec2(18.0 * px, 0.0)).rgb;
    col = mix(col, max(col, echo), ghost * (0.3 + env));

    // dropouts: short blocky patches of the picture itself, shifted and brightened where the tape lost contact
    float dropRow = floor(gl_FragCoord.y / max(1.0, resolution.y / 180.0));
    float dropTime = floor(loopFrame * 0.5);
    float dropOn = step(1.0 - 0.01 * vhsDropouts, hash2(vec2(dropRow, dropTime)));
    float dropStart = hash2(vec2(dropRow + 3.0, dropTime));
    float dropLen = 0.04 + 0.15 * hash2(vec2(dropRow + 9.0, dropTime));
    float inDrop = step(dropStart, uv.x) * step(uv.x, dropStart + dropLen);
    vec3 dropCol = texture2D(texture, uv + vec2(0.03, 0.0)).rgb * 1.8 + (hash2(gl_FragCoord.xy + loopFrame) - 0.5) * 0.15;
    col = mix(col, dropCol, dropOn * inDrop * 0.85);

    // tape hiss: horizontally streaked noise
    float hiss = hash2(vec2(floor(gl_FragCoord.x * 0.25), gl_FragCoord.y + loopFrame * 7.0));
    col += (hiss - 0.5) * 0.12 * vhsNoise;

    col *= 1.0 - scanlines * (0.5 + 0.5 * sin(gl_FragCoord.y * 3.14159));

    vec2 c = gl_FragCoord.xy / resolution.xy - 0.5;
    col *= 1.0 - vignette * dot(c, c) * 2.0;

    // film grain, strongest in the shadows
    float lum = dot(col, vec3(0.333));
    col += (hash2(gl_FragCoord.xy + loopFrame * vec2(1.7, 3.1)) - 0.5) * grain * (1.2 - lum);

    gl_FragColor = vec4(col, 1.0);
}
