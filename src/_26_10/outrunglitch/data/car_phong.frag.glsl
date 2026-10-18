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

varying vec3 vEcPos;
varying vec3 vEcNormal;
varying vec4 vColor;

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

    vec3 rgb = (totalAmbient + totalDiffuse) * vColor.rgb + totalSpecular * specColor;
    gl_FragColor = vec4(min(rgb, vec3(1.0)), vColor.a);
}
