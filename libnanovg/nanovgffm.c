#include "glad/include/glad/gl.h"
#define NANOVG_GL3_IMPLEMENTATION
#include "nanovg/src/nanovg.h"
#include "nanovg/src/nanovg_gl.h"

NVGcontext *nvgCreateContext(int flags)
{
    int version = gladLoaderLoadGL();
    if (version == 0)
    {
        return NULL;
    }
    return nvgCreateGL3(flags);
}

void nvgDeleteContext(NVGcontext *ctx)
{
    nvgDeleteGL3(ctx);
}