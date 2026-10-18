#define PROCESSING_LIGHT_SHADER

uniform int lightCount;
uniform vec4 lightPosition[8];
uniform vec3 lightNormal[8];
uniform vec3 lightAmbient[8];
uniform vec3 lightDiffuse[8];
uniform vec3 lightSpecular[8];
uniform vec3 lightFalloff[8];
uniform vec2 lightSpot[8];

// material specular is set per draw call because retained shapes bake theirs in at creation
uniform vec3 specColor;
uniform float specShininess;

uniform float reflAmount;
uniform float aoAmount;
uniform vec3 skyTop;
uniform vec3 skyHorizon;

varying vec3 vEcPos;
varying vec3 vEcNormal;
varying vec4 vColor;
varying float vHeight;

float falloffFactor(vec3 lightPos, vec3 vertPos, vec3 coeff) {
    vec3 lpv = lightPos - vertPos;
    vec3 dist = vec3(1.0, length(lpv), dot(lpv, lpv));
    return 1.0 / dot(dist, coeff);
}

float spotFactor(vec3 lightPos, vec3 vertPos, vec3 lightNorm, float minCos, float spotExp) {
    vec3 lpv = normalize(lightPos - vertPos);
    vec3 nln = -1.0 * lightNorm;
    float spotCos = dot(nln, lpv);
    return spotCos <= minCos ? 0.0 : pow(spotCos, spotExp);
}

float lambertFactor(vec3 lightDir, vec3 vecNormal) {
    return max(0.0, dot(lightDir, vecNormal));
}

float blinnPhongFactor(vec3 lightDir, vec3 vertPos, vec3 vecNormal, float shine) {
    vec3 np = normalize(vertPos);
    vec3 ldp = normalize(lightDir - np);
    return pow(max(0.0, dot(ldp, vecNormal)), shine);
}

void main() {
    vec3 n = normalize(vEcNormal);
    vec3 totalAmbient = vec3(0.0);
    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for (int i = 0; i < 8; i++) {
        if (i >= lightCount) break;

        vec3 lightPos = lightPosition[i].xyz;
        bool isDir = lightPosition[i].w < 1.0;
        float spotCos = lightSpot[i].x;
        float spotExp = lightSpot[i].y;

        vec3 lightDir;
        float falloff;
        if (isDir) {
            falloff = 1.0;
            lightDir = -1.0 * lightNormal[i];
        } else {
            falloff = falloffFactor(lightPos, vEcPos, lightFalloff[i]);
            lightDir = normalize(lightPos - vEcPos);
        }
        float spotf = spotExp > 0.0 ? spotFactor(lightPos, vEcPos, lightNormal[i], spotCos, spotExp) : 1.0;

        totalAmbient += lightAmbient[i] * falloff;
        totalDiffuse += lightDiffuse[i] * falloff * spotf * lambertFactor(lightDir, n);
        totalSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, vEcPos, n, specShininess);
    }

    // lower body sits in shadow so the shape reads from the side too
    float ao = mix(1.0 - aoAmount, 1.0, smoothstep(0.25, 0.95, vHeight));
    vec3 rgb = (totalAmbient + totalDiffuse) * vColor.rgb * ao + totalSpecular * specColor;

    // fake sky reflection: sky colour above, a bright horizon band, stronger at grazing angles
    vec3 view = vec3(0.0, 0.0, -1.0);
    vec3 r = reflect(view, n);
    float up = -r.y;
    float fres = 0.12 + 0.88 * pow(1.0 - max(dot(n, -view), 0.0), 2.0);
    vec3 env = mix(skyHorizon * 0.25, skyTop, smoothstep(-0.1, 0.7, up));
    env += skyHorizon * exp(-abs(up) * 7.0) * 0.9;
    rgb += env * fres * reflAmount;

    // lamp lenses glow on their own: tail lights are pure red, headlights warm white
    bool tail = vColor.r > 0.98 && vColor.g < 0.2 && vColor.b < 0.2;
    bool head = vColor.r > 0.98 && vColor.g > 0.95 && vColor.b > 0.8 && vColor.b < 0.95;
    if (tail) rgb = vec3(1.0, 0.1, 0.08);
    if (head) rgb = vec3(1.0, 0.96, 0.8);
    gl_FragColor = vec4(min(rgb, vec3(1.0)), vColor.a);
}
