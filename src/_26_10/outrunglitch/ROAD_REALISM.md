# OutrunGlitch: road realism plan (not built yet)

The car doesn't feel like it is on the road. It sits in the middle of the asphalt in a flat side view, and nothing ties it to the surface. The lane dashes are gone, so the asphalt is a plain gradient.

## Why it floats

1. **Weak contact shadow.** `drawShadow()` draws one soft ellipse wider than the car. A real shadow is darkest right under the tyres.
2. **No road cue.** Nothing on the asphalt says "surface": no lines, no texture, no reflection.
3. **No light interaction.** The headlights and tail lights don't touch the road, and the body colour doesn't spill onto it.

## Plan, in order of value

| # | Change | Where | Notes |
|---|---|---|---|
| 1 | Tight contact shadows | `drawShadow()` | a small dark ellipse under each tyre, plus the wide soft one under the body. The shadow is darkest at the wheels. |
| 2 | Wet road reflection | a new pass after the car, before the post pass | a vertically flipped, blurred copy of the car under it, fading downwards, tinted by the palette. Probably rendered into `glowBuf`-style buffer and drawn with `blendMode(ADD)`. |
| 3 | Light spill on the road | `drawLightBeams()` and a new glow under the car | a faint red glow under the body, and the beam touching the asphalt ahead of the car. |
| 4 | Tyre dust or spray | the exhaust particle system | small particles at the wheels. The most work and the least convincing, so last. |

## Controls to add

A `road` folder, for example:
- `contact shadow` (opacity)
- `reflection` (opacity)
- `reflection fade` (how fast it dissolves)
- `reflection blur`
- `light spill` (opacity)

## Open questions

- Should the reflection show the whole car or only the lower half?
- Should it be glossy (a clear mirror image) or rough (a faint smear)?
- Does the lane marking come back in some form, for example a single faint edge line near the car, or does the asphalt stay plain?
