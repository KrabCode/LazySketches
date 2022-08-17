
```
ffmpeg -y -an -framerate 30 -start_number_range 50 -i %01d.jpg "..\..\recorded videos\pixelsort.mp4"
```

- -y: yes to overwrite
- -an: disable audio
- -framerate: framerate
- -start_number_range: how far to look for first file in sequence
- -i: input file(s)
- last part is output file path including desired extension 