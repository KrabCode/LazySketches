# OutrunGlitch: story plan (not built yet)

The video is one 60 second pass: 3600 frames at 60 fps, no loop. The TV flashes between runs, so nothing needs to line up with the first frame.

Right now there is no story. The car sits in the middle, the scene scrolls at a constant speed and the glitch bursts fire at a steady 3 per second. This file is the plan for what could be keyed to `frameCount` once we want it.

## What can be driven by the timeline

| Thing | Where it lives | Notes |
|---|---|---|
| Car x position | `updateCar()`, `x` slider | overtake from the left, drive off to the right |
| Wanted stars | `drawWantedStars()`, `level` | 0 to 5, filling left to right |
| Tooltip | `drawTooltip()`, `enabled` | pop in, maybe fade |
| Glitch bursts | `postPass()`, `bursts per second`, `amount` | frequency was explicitly kept steady for now |
| Palette | `freewayBackground()`, `palette` | blob colours could shift per section |
| Road speed | `freeway/speed` | slow cruise to full speed |
| Exhaust smoke | `exhaust/puffs per second` | heavier when the car is pushed |
| Light beams | `light beams/headlight beam` | brighter late in the chase |
| Road lighting | `road/headlight wash`, `road/tail light wash` | pulse with the chase; a brake flash needs the `brake` idea from `ROAD_REALISM.md` |
| Title | `drawTitle()` | glitch in at the start, fade at the end |

Nothing is wired to time yet. A small helper like `float seg(float startSec, float endSec)` that returns 0 to 1 across a time range would cover most of it.

## First draft of the minute

| Time | Frames | What happens | Stars |
|---|---|---|---|
| 0 to 8 s | 0 to 480 | empty road, title glitches in | 0 |
| 8 to 16 s | 480 to 960 | the car overtakes from the left and settles in the middle | 1 |
| 16 to 40 s | 960 to 2400 | cruising, stars climb, speed builds slowly | 1 to 3 |
| 40 to 50 s | 2400 to 3000 | the "Press X to go ALL IN" tooltip appears, stars reach 5 | 5 |
| 50 to 60 s | 3000 to 3600 | the car drives off to the right, the road empties | 5 |

## Open questions

- Should the last frames be a clean end for the TV flash, or just cut?
- Should glitch intensity follow the stars, or stay steady?
- Should the palette change during the minute, or stay one palette?
- Does the tooltip pop in once, or pulse?
