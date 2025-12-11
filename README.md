# NanoVG-FFM

> PROJECT IS EXPERIMENTAL, ONLY WORKS IN AMD64 LINUX

**Only compatible with Java25+**

NanoVG OpenGL3 api using Java FFM.
The library is compatible with existing LWJGL projects and with GLFW/GL-FFM projects.
The goal of this project is to show that's possible to only use java tooling (FFM and jextract) to use external libraries like Gl, GLFW, GLEW etc... in a future-proof way, without Unsafe.

I don't think the performances exactly matches tho, it might be slightly slower.
From my observation on my computer (9950x3d, 5070Ti, 64gb RAM) and on this demo, it seems that the p99 of frames are:
- LWJGL NanoVG : ~75 microseconds (it uses JNI under the hood, it needs special C code to interop with the JVM, and uses Unsafe)
- FFM NanoVG : ~120 microseconds (it uses modern api like FFM, does not uses Unsafe, and load dynamic linked libraries to interop with native C codes, like OpenGL or NanoVG.)

So it's like 60% slower on p99.
Note: it might be because i'm not allocating memory correctly, and the number of allocations might differ.

## Development

For now it's just a proof of concept.
