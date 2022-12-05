uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

uniform vec3 atmoColor;
uniform vec3 dirtColor;
uniform vec3 camPos;
uniform float atmoDistance;
uniform float planetRadius;
uniform float rayStepSize;

uniform int   maxSteps;
uniform float maxDist;
uniform float surfDist;

#define iResolution resolution
#define fragCoord gl_FragCoord
#define fragColor gl_FragColor

#define MAX_STEPS maxSteps
#define MAX_DISTANCE maxDist
#define SURFACE_DISTANCE surfDist

void main()
{
    vec2 uv = (fragCoord.xy - iResolution.xy / 2) / iResolution.xy;
    vec2 pixelSize = vec2(1.0 / iResolution.x, 1.0 / iResolution.y);
    // Compute the lighting
    vec3 lightDir = normalize(vec3(0.1*sin(time), 0.1, 0.1*cos(time)));
    // Compute the ray direction
    vec3 rayDir = normalize(vec3(uv, 1.0))*rayStepSize;
    // Perform ray marching
    float totalDistance = 0.0;
    float closestDistance = 9999.0;
    float lightAtClosestDistance = 0.;
    vec3 foundColor = vec3(0);
    for (int i = 0; i < MAX_STEPS; i++)
    {
        vec3 rayPos = camPos + rayDir * totalDistance;
        float distanceToSurface = length(rayPos) - planetRadius;
        closestDistance = min(closestDistance, distanceToSurface);
        vec3 normal = normalize(rayPos);
        float light = max(0.0, dot(normal, lightDir));
        if (distanceToSurface < SURFACE_DISTANCE)
        {
            // Set the fragment color
            foundColor = vec3(dirtColor * light);
            break;
        }else{
            lightAtClosestDistance = light;
        }
        totalDistance += distanceToSurface;
        if (totalDistance >= MAX_DISTANCE)
        {
            break;
        }
    }

    float atmo = smoothstep(atmoDistance, 0., closestDistance);
//    foundColor = mix(foundColor, atmoColor, atmo*lightAtClosestDistance);
    foundColor += atmo * atmoColor * lightAtClosestDistance;
    gl_FragColor = vec4(foundColor, 1.0);
}