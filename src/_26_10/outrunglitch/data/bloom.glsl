uniform sampler2D texture;
uniform vec2 resolution;
uniform float bloom;

// blurs the light mask in rings so the glow follows the real lens shapes
void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float px = 1.0 / resolution.x;
    vec3 glow = texture2D(texture, uv).rgb;
    for (int ring = 0; ring < 3; ring++) {
        float radius = (ring == 0 ? 6.0 : ring == 1 ? 16.0 : 36.0) * px;
        for (int i = 0; i < 12; i++) {
            float a = (float(i) + 0.5 * float(ring)) * 0.5236;
            vec2 off = vec2(cos(a), sin(a) * resolution.x / resolution.y) * radius;
            glow += texture2D(texture, uv + off).rgb / (1.0 + float(ring) * 1.5);
        }
    }
    gl_FragColor = vec4(glow * bloom / 12.0, 1.0);
}
