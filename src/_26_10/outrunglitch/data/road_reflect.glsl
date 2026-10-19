uniform sampler2D texture;
uniform vec2 resolution;
uniform float groundY;   // ground line, 0 = bottom of the screen, 1 = top
uniform float opacity;
uniform float fade;      // how quickly the reflection dissolves going down
uniform float blur;      // blur radius at the ground line, grows with distance below it
uniform vec3 tint;
uniform float lampBoost; // extra brightness for the lamp lenses so their reflection survives the blur

vec3 lamps(vec2 p) {
    vec3 c = texture2D(texture, p).rgb;
    return c * (1.0 + lampBoost * smoothstep(0.9, 1.0, max(c.r, max(c.g, c.b))));
}

// wet asphalt: the mirrored car is smeared more and more the further it is from the tyres
void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float px = resolution.y / 720.0 / resolution.x;
    float d = max(groundY - uv.y, 0.0);
    float radius = (blur + d * 60.0 * blur) * px;

    vec3 sum = vec3(0.0);
    float wsum = 0.0;
    for (int ring = 0; ring < 3; ring++) {
        float r = radius * float(ring + 1) / 3.0;
        for (int i = 0; i < 8; i++) {
            float a = (float(i) + 0.5 * float(ring)) * 0.7854;
            vec2 off = vec2(cos(a), sin(a) * resolution.x / resolution.y) * r;
            float w = 1.0 / (1.0 + float(ring));
            sum += lamps(uv + off) * w;
            wsum += w;
        }
    }
    vec3 col = (sum / wsum + lamps(uv)) * 0.5;

    float f = exp(-d * fade * 8.0);
    col = mix(col, col * tint, 0.5);
    gl_FragColor = vec4(col * f * opacity, 1.0);
}
