#include <GL/glew.h>
#define NANOVG_GL3_IMPLEMENTATION
#include "nanovg/src/nanovg.h"
#include "nanovg/src/nanovg_gl.h"

NVGcontext *nvgCreateContext(int flags)
{
    GLenum err = glewInit();
    if (GLEW_OK != err)
    {
        return NULL;
    }
    return nvgCreateGL3(flags);
}

void nvgDeleteContext(NVGcontext *ctx)
{
    nvgDeleteGL3(ctx);
}