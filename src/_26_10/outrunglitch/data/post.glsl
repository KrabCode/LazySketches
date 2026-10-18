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

float hash(float n) { return fract(sin(n * 12.9898) * 43758.5453); }
float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

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
    float stripRows = 70.0;
    float strip = floor(uv.y * stripRows + hash(tick) * 10.0);
    float slide = (hash2(vec2(strip, tick)) - 0.5) * step(0.72, hash2(vec2(strip + 5.0, tick)));
    uv.x += slide * tear * env;

    // every scanline jitters a hair during a burst
    float lineId = floor(gl_FragCoord.y);
    uv.x += (hash2(vec2(lineId, loopFrame)) - 0.5) * lineJitter * env;

    // a rolling tracking band, like a worn tape
    float bandPos = fract(loopPhase);
    float bandDist = abs(uv.y - bandPos);
    float band = smoothstep(0.06, 0.0, bandDist) * tracking * (0.3 + env);
    uv.x += (hash2(vec2(lineId, floor(loopFrame * 0.5))) - 0.5) * 0.02 * band;

    // occasional vertical hold slip
    uv.y += sin(uv.y * 3.0 + TAU * loopPhase * 40.0) * 0.004 * env * step(0.8, hash(tick * 9.1));

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

    // horizontal ghost echo
    vec3 echo = texture2D(texture, uv - vec2(18.0 * px, 0.0)).rgb;
    col = mix(col, max(col, echo), ghost * (0.3 + env));

    col *= 1.0 - scanlines * (0.5 + 0.5 * sin(gl_FragCoord.y * 3.14159));

    vec2 c = gl_FragCoord.xy / resolution.xy - 0.5;
    col *= 1.0 - vignette * dot(c, c) * 2.0;

    // film grain, strongest in the shadows
    float lum = dot(col, vec3(0.333));
    col += (hash2(gl_FragCoord.xy + loopFrame * vec2(1.7, 3.1)) - 0.5) * grain * (1.2 - lum);

    gl_FragColor = vec4(col, 1.0);
}
