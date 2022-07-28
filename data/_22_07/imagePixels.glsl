uniform sampler2D texture;

uniform vec2 resolution;
uniform float time;
uniform float strength;
uniform vec2 mouse;
uniform vec3 targetColorRGB;
uniform float targetSmoothstepLow;
uniform float targetSmoothstepHigh;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy; // coordinate with 0,0 at bottom left, and 1,1 at top right
    vec2 cv = (gl_FragCoord.xy - 0.5*resolution.xy) / resolution.y;
    vec2 mv = mouse.xy / resolution.xy;
//    float angleFromMouse = atan(mv.y-uv.y, mv.x-uv.x);
//    float distFromMouse = length(mv-uv);
    float a = atan(cv.y, cv.x);

    vec2 offset = strength/resolution.x*vec2(cos(a), sin(a)); // vec2(displacement*cos(angleFromMouse), displacement*sin(angleFromMouse)) / resolution.xy; // the rotating offset to sample a neighbour at
    vec4 thisPixelColor = texture2D(texture, uv);
    vec4 nearPixelColor = texture2D(texture, uv + offset);
    float distanceToTargetColor = distance(thisPixelColor.rgb, targetColorRGB.rgb);
    float getOverwrittenByNeighbourLerpAmt = smoothstep(targetSmoothstepLow, targetSmoothstepHigh, distanceToTargetColor);
    vec4 finalColor = mix(thisPixelColor, nearPixelColor, getOverwrittenByNeighbourLerpAmt); // lerp between the two colors, 0.5 specifies how quickly the effect fades into the background
    gl_FragColor = vec4(finalColor.rgb, 1.);
}