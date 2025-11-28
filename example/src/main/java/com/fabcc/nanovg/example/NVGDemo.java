package com.fabcc.nanovg.example;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Path;

import com.fabcc.nanovg.lib.NVG;
import com.fabcc.nanovg.lib.NVGcolor;

public class NVGDemo {

    private MemorySegment fontName;
    private MemorySegment title;
    private MemorySegment nvgContext;
    private int monoFont;

    private MemorySegment colorA;

    public NVGDemo(Arena arena, String title, MemorySegment nvgContext) {
        this.fontName = arena.allocateFrom("sans-bold");
        this.title = arena.allocateFrom(title);
        this.nvgContext = nvgContext;
        String workdir = System.getProperty("user.dir");
        var pathToRoboto = Path.of(
                workdir,
                "../example/src/main/resources/RobotoMono-Medium.ttf").toString();
        System.out.println("Path to resource " + pathToRoboto);
        MemorySegment filePath = arena.allocateFrom(pathToRoboto);
        this.monoFont = NVG.nvgCreateFont(nvgContext, this.fontName, filePath);
        if (this.monoFont == -1) {
            throw new IllegalStateException("Cant create nvgFont from file " + filePath.getString(0));
        }
        System.out.println("Monotfond id = " + this.monoFont);
        this.colorA = NVGcolor.allocate(arena);
    }

    public int getMonoFont() {
        return monoFont;
    }

    /**
     * Render the demo scene.
     * 
     * @param cx        Cursor x position.
     * @param cy        Cursor y position.
     * @param width     Window width.
     * @param height    Window height.
     * @param deltaTime Time elapsed since last frame in seconds.
     */
    public void renderDemo(double cx, double cy, float width, float height, double deltaTime) {
        try (Arena frameArena = Arena.ofConfined()) { // Confine allocations to this frame
            drawColorwheel(frameArena, width - 300, height - 300, 250.0f, 250.0f, (float) deltaTime);
            drawWindow(frameArena, 0, 0, width, height);
            drawCursor(frameArena, (float) cx, (float) cy);
        }
    }

    /**
     * Render the demo scene with less call to memory allocation.
     * By using a pre-allocated memory chunk for temporary allocations, we can
     * reduce
     * number of allocations per frame.
     * 
     * @param allocatedChunk Pre-allocated memory chunk for temporary allocations.
     * @param cx             Cursor x position.
     * @param cy             Cursor y position.
     * @param width          Window width.
     * @param height         Window height.
     * @param deltaTime      Time elapsed since last frame in seconds.
     */
    public void renderDemoLowMemalloc(
            SlicingArena slicingArena,
            double cx, double cy,
            float width, float height,
            double deltaTime) {
        drawColorwheel(slicingArena, width - 300, height - 300, 250.0f, 250.0f, (float) deltaTime);
        drawWindow(slicingArena, 0, 0, width, height);
        drawCursor(slicingArena, (float) cx, (float) cy);
    }

    private void drawCursor(Arena arena, float cx, float cy) {
        NVG.nvgSave(nvgContext);

        NVG.nvgBeginPath(nvgContext);
        NVG.nvgCircle(nvgContext, cx, cy, 10.0f);
        NVG.nvgFillColor(nvgContext, rgba(255, 192, 0, 255));
        NVG.nvgFill(nvgContext);

        NVG.nvgBeginPath(nvgContext);
        NVG.nvgCircle(nvgContext, cx, cy, 6.0f);
        NVG.nvgFillColor(nvgContext, rgba(32, 32, 32, 255));
        NVG.nvgFill(nvgContext);

        NVG.nvgRestore(nvgContext);
    }

    /**
     * Draw a window with a title bar.
     * 
     * @param arena Arena for memory allocation.
     * @param x     Starting x position of the window.
     * @param y     Starting y position of the window.
     * @param w     Width of the window.
     * @param h     Height of the window.
     */
    private void drawWindow(Arena arena, float x, float y, float w, float h) {
        float cornerRadius = 3.0f;
        MemorySegment shadowPaint;
        MemorySegment headerPaint;

        NVG.nvgSave(nvgContext);

        // Window
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgRoundedRect(nvgContext, x, y, w, h, cornerRadius);
        NVG.nvgFillColor(nvgContext, rgba(28, 30, 34, 192));
        // NVG.nvgFillColor(nvgContext, rgba(0,0,0,128));
        NVG.nvgFill(nvgContext);

        // Drop shadow
        shadowPaint = NVG.nvgBoxGradient(arena, nvgContext, x, y + 2, w, h, cornerRadius * 2, 10,
                rgba(0, 0, 0, 128),
                rgba(0, 0, 0, 0));
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgRect(nvgContext, x - 10, y - 10, w + 20, h + 30);
        NVG.nvgRoundedRect(nvgContext, x, y, w, h, cornerRadius);
        NVG.nvgPathWinding(nvgContext, NVG.NVG_HOLE());
        NVG.nvgFillPaint(nvgContext, shadowPaint);
        NVG.nvgFill(nvgContext);

        // Header
        headerPaint = NVG.nvgLinearGradient(arena, nvgContext, x, y, x, y + 15,
                rgba(255, 255, 255, 8),
                rgba(0, 0, 0, 16));
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgRoundedRect(nvgContext, x + 1, y + 1, w - 2, 30, cornerRadius - 1);
        NVG.nvgFillPaint(nvgContext, headerPaint);
        NVG.nvgFill(nvgContext);
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgMoveTo(nvgContext, x + 0.5f, y + 0.5f + 30);
        NVG.nvgLineTo(nvgContext, x + 0.5f + w - 1, y + 0.5f + 30);
        NVG.nvgStrokeColor(nvgContext, rgba(0, 0, 0, 32));
        NVG.nvgStroke(nvgContext);

        NVG.nvgFontSize(nvgContext, 15.0f);
        NVG.nvgFontFace(nvgContext, fontName);
        NVG.nvgTextAlign(nvgContext, NVG.NVG_ALIGN_CENTER() | NVG.NVG_ALIGN_MIDDLE());

        NVG.nvgFontBlur(nvgContext, 2);
        NVG.nvgFillColor(nvgContext, rgba(0, 0, 0, 128));
        NVG.nvgText(nvgContext, x + w / 2, y + 16 + 1, title, MemorySegment.NULL);

        NVG.nvgFontBlur(nvgContext, 0);
        NVG.nvgFillColor(nvgContext, rgba(220, 220, 220, 160));
        NVG.nvgText(nvgContext, x + w / 2, y + 16, title, MemorySegment.NULL);

        NVG.nvgRestore(nvgContext);
    }

    private void drawColorwheel(Arena arena, float x, float y, float w, float h, float t) {
        int i;
        float r0, r1, ax, ay, bx, by, cx, cy, aeps, r;
        float hue = (float) Math.sin(t * 0.12f);
        MemorySegment paint;

        NVG.nvgSave(nvgContext);

        /*
         * nvgBeginPath(vg);
         * nvgRect(vg, x,y,w,h);
         * nvgFillColor(vg, nvgRGBA(255,0,0,128));
         * nvgFill(vg);
         */

        cx = x + w * 0.5f;
        cy = y + h * 0.5f;
        r1 = (w < h ? w : h) * 0.5f - 5.0f;
        r0 = r1 - 20.0f;
        aeps = 0.5f / r1; // half a pixel arc length in radians (2pi cancels out).

        for (i = 0; i < 6; i++) {
            float a0 = (float) i / 6.0f * NVG.NVG_PI() * 2.0f - aeps;
            float a1 = (float) (i + 1.0f) / 6.0f * NVG.NVG_PI() * 2.0f + aeps;
            NVG.nvgBeginPath(nvgContext);
            NVG.nvgArc(nvgContext, cx, cy, r0, a0, a1, NVG.NVG_CW());
            NVG.nvgArc(nvgContext, cx, cy, r1, a1, a0, NVG.NVG_CCW());
            NVG.nvgClosePath(nvgContext);
            ax = cx + (float) Math.cos(a0) * (r0 + r1) * 0.5f;
            ay = cy + (float) Math.sin(a0) * (r0 + r1) * 0.5f;
            bx = cx + (float) Math.cos(a1) * (r0 + r1) * 0.5f;
            by = cy + (float) Math.sin(a1) * (r0 + r1) * 0.5f;
            paint = NVG.nvgLinearGradient(arena, nvgContext, ax, ay, bx, by,
                    NVG.nvgHSLA(arena, a0 / (NVG.NVG_PI() * 2f), 1.0f, 0.55f, 255),
                    NVG.nvgHSLA(arena, a1 / (NVG.NVG_PI() * 2f), 1.0f, 0.55f, 255));
            NVG.nvgFillPaint(nvgContext, paint);
            NVG.nvgFill(nvgContext);
        }

        NVG.nvgBeginPath(nvgContext);
        NVG.nvgCircle(nvgContext, cx, cy, r0 - 0.5f);
        NVG.nvgCircle(nvgContext, cx, cy, r1 + 0.5f);
        NVG.nvgStrokeColor(nvgContext, rgba(0, 0, 0, 64));
        NVG.nvgStrokeWidth(nvgContext, 1.0f);
        NVG.nvgStroke(nvgContext);

        // Selector
        NVG.nvgSave(nvgContext);
        NVG.nvgTranslate(nvgContext, cx, cy);
        NVG.nvgRotate(nvgContext, hue * NVG.NVG_PI() * 2);

        // Marker on
        NVG.nvgStrokeWidth(nvgContext, 2.0f);
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgRect(nvgContext, r0 - 1, -3, r1 - r0 + 2, 6);
        NVG.nvgStrokeColor(nvgContext, rgba(255, 255, 255, 192));
        NVG.nvgStroke(nvgContext);

        paint = NVG.nvgBoxGradient(arena, nvgContext, r0 - 3, -5, r1 - r0 + 6, 10, 2, 4,
                rgba(0, 0, 0, 128),
                rgba(0, 0, 0, 0));
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgRect(nvgContext, r0 - 2 - 10, -4 - 10, r1 - r0 + 4 + 20, 8 + 20);
        NVG.nvgRect(nvgContext, r0 - 2, -4, r1 - r0 + 4, 8);
        NVG.nvgPathWinding(nvgContext, NVG.NVG_HOLE());
        NVG.nvgFillPaint(nvgContext, paint);
        NVG.nvgFill(nvgContext);

        // Center triangle
        r = r0 - 6;
        ax = (float) Math.cos(120.0f / 180.0f * NVG.NVG_PI()) * r;
        ay = (float) Math.sin(120.0f / 180.0f * NVG.NVG_PI()) * r;
        bx = (float) Math.cos(-120.0f / 180.0f * NVG.NVG_PI()) * r;
        by = (float) Math.sin(-120.0f / 180.0f * NVG.NVG_PI()) * r;
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgMoveTo(nvgContext, r, 0);
        NVG.nvgLineTo(nvgContext, ax, ay);
        NVG.nvgLineTo(nvgContext, bx, by);
        NVG.nvgClosePath(nvgContext);
        paint = NVG.nvgLinearGradient(arena, nvgContext, r, 0, ax, ay,
                NVG.nvgHSLA(arena, hue, 1.0f, 0.5f, 255),
                rgba(255, 255, 255, 255));
        NVG.nvgFillPaint(nvgContext, paint);
        NVG.nvgFill(nvgContext);
        paint = NVG.nvgLinearGradient(arena, nvgContext, (r + ax) * 0.5f, (0 + ay) * 0.5f, bx, by,
                rgba(0, 0, 0, 0),
                rgba(0, 0, 0, 255));
        NVG.nvgFillPaint(nvgContext, paint);
        NVG.nvgFill(nvgContext);
        NVG.nvgStrokeColor(nvgContext, rgba(0, 0, 0, 64));
        NVG.nvgStroke(nvgContext);

        // Select circle on triangle
        ax = (float) Math.cos(120.0f / 180.0f * NVG.NVG_PI()) * r * 0.3f;
        ay = (float) Math.sin(120.0f / 180.0f * NVG.NVG_PI()) * r * 0.4f;
        NVG.nvgStrokeWidth(nvgContext, 2.0f);
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgCircle(nvgContext, ax, ay, 5);
        NVG.nvgStrokeColor(nvgContext, rgba(255, 255, 255, 192));
        NVG.nvgStroke(nvgContext);

        paint = NVG.nvgRadialGradient(arena, nvgContext, ax, ay, 7, 9, rgba(0, 0, 0, 64),
                rgba(0, 0, 0, 0));
        NVG.nvgBeginPath(nvgContext);
        NVG.nvgRect(nvgContext, ax - 20, ay - 20, 40, 40);
        NVG.nvgCircle(nvgContext, ax, ay, 7);
        NVG.nvgPathWinding(nvgContext, NVG.NVG_HOLE());
        NVG.nvgFillPaint(nvgContext, paint);
        NVG.nvgFill(nvgContext);
        NVG.nvgRestore(nvgContext);
        NVG.nvgRestore(nvgContext);
    }

    protected MemorySegment rgba(int r, int g, int b, int a) {
        return rgba(r, g, b, a, colorA);
    }

    protected MemorySegment rgba(int r, int g, int b, int a, MemorySegment source) {
        NVGcolor.r(source, r / 255.0f);
        NVGcolor.g(source, g / 255.0f);
        NVGcolor.b(source, b / 255.0f);
        NVGcolor.a(source, a / 255.0f);
        return source;
    }

}
