#!/usr/bin/env bash
set -e
rm src/main/java/com/fabcc/nanovg/lib/NVG.java || true
jextract -t com.fabcc.nanovg.lib --output src/main/java \
    -l ./libnanovg/libnanovgffm.so \
    --header-class-name "NVG" \
    @includes.txt \
    ./libnanovg/nanovg/src/nanovg.h \
    ./libnanovg/nanovg/src/nanovg_gl.h \
    ./libnanovg/nanovgffm.c
