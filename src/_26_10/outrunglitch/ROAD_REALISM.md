# OutrunGlitch: road realism

The car didn't feel like it was on the road: a flat side view on a plain gradient, with a weak shadow and no light interaction. Most of this is now built. Everything has a slider in the `road` folder.

## What is built

| Change | Where | Sliders |
|---|---|---|
| Wet road reflection: the car mirrored under the ground line, blurred more and faded with distance, tinted by the palette. Lamp lenses are boosted so they survive the blur. | `drawRoadReflection()`, `data/road_reflect.glsl` | `reflection` 1.2, `reflection fade` 0.4, `reflection blur` 4, `reflection lights` 4 |
| Road shadow: a dark strip on the asphalt under the body | asphalt block of `data/bg_freeway.glsl`, `roadShading()` | `road shadow` 0.7 |
| Headlight wash: a soft warm glow on the road ahead that widens and fades | same | `headlight wash` 1.5, `wash start` 0.7, `wash falloff` 1.2, `wash width` 0.1, `wash y` 0 |
| Tail light wash: a dark red wash behind the car, rotatable around the tail light | same | `tail light wash` 6.1, `tail wash color`, `tail wash start` 0.57, `tail wash falloff` 5, `tail wash width` 0.054, `tail wash spread` 7.6, `tail wash y` 0.012, `tail wash angle` 15, `tail wash pivot` -0.75 |

The shading is done in the road shader, not by drawing shapes over the road. All of it follows `car/x`, `car/size`, `car/road y` and `flip direction`.

The headlight lens no longer has the grey reflector patch, so it is one bright colour.

## What didn't work

- **Per-tyre contact shadows** drawn as ellipses: near and far wheels land at different screen x because of the yaw, so each blob sat beside its tyre.
- **Ellipse light pools** and **additive wedge quads** on the road: visible rings, hard edges, and they looked like floating discs.
- **A narrow streak** copied from a wet-road photo: not what we wanted. The soft wash is the version that stayed.

## Not built

- **Tyre dust or spray:** small particles at the wheels. The most work and the least convincing.
- **Brake light:** the tail lights are always at full red now. A running-light level with a brighter braking state is more realistic. A `brake` slider could drive the lens and the tail wash together, and the story could key it to a moment.
- **A single faint lane or edge line near the car**, if the asphalt ever feels too plain.

## Open questions

- Should the reflection show the whole car or only the lower half? (Currently the whole car.)
- Glossy mirror or rough smear? (Currently a blurred smear with sharper lights.)
