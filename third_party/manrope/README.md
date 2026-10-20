# Manrope

The app's font. Licensed under the SIL Open Font License 1.1 (`OFL.txt`).

Source: `ofl/manrope/Manrope[wght].ttf` from https://github.com/google/fonts.
The four static weights in `app/src/main/res/font/` were cut from that variable font with:

    fonttools varLib.instancer 'Manrope[wght].ttf' wght=<400|600|700|800> -o manrope_<weight>.ttf
