package com.fabcc.nanovg.example.lwjgl;

import java.lang.foreign.Arena;
import java.nio.file.Path;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import com.fabcc.nanovg.example.SlicingArena;

public class NVGDemoLWJGL {

    static final NVGColor colorA = NVGColor.create(),
            colorB = NVGColor.create(),
            colorC = NVGColor.create();

    static final NVGPaint paintA = NVGPaint.create(),
            paintB = NVGPaint.create(),
            paintC = NVGPaint.create();

    private long nvgContext;
    private String title;
    private String fontName;
    private int monoFont;

    public NVGDemoLWJGL(long nvgContext, String title) {
        this.nvgContext = nvgContext;
        this.title = title;
        this.fontName = "sans-bold";
        String workdir = System.getProperty("user.dir");
        var pathToRoboto = Path.of(
                workdir,
                "../example/src/main/resources/RobotoMono-Medium.ttf").toString();
        this.monoFont = NanoVG.nvgCreateFont(nvgContext, this.fontName, pathToRoboto);
        if (this.monoFont == -1) {
            throw new IllegalStateException("Cant create nvgFont from file " + pathToRoboto);
        }
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
        NanoVG.nvgSave(nvgContext);

        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgCircle(nvgContext, cx, cy, 10.0f);

        NanoVG.nvgFillColor(nvgContext,
                rgba(255, 192, 0, 255, colorA));
        NanoVG.nvgFill(nvgContext);

        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgCircle(nvgContext, cx, cy, 6.0f);
        NanoVG.nvgFillColor(nvgContext, rgba(32, 32, 32, 255, colorA));
        NanoVG.nvgFill(nvgContext);

        NanoVG.nvgRestore(nvgContext);
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

        NanoVG.nvgSave(nvgContext);

        // Window
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgRoundedRect(nvgContext, x, y, w, h, cornerRadius);
        NanoVG.nvgFillColor(nvgContext, rgba(28, 30, 34, 192, colorA));
        // NanoVG.nvgFillColor(nvgContext,
        // rgba(,0 ,(b )(b, colorA NanoVG.nvgFill(nvgContext);

        // Drop shadow
        NanoVG.nvgBoxGradient(nvgContext, x, y + 2, w, h, cornerRadius * 2, 10,
                rgba(0, 0, 0, 128, colorB),
                rgba(0, 0, 0, 0, colorB), paintA);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgRect(nvgContext, x - 10, y - 10, w + 20, h + 30);
        NanoVG.nvgRoundedRect(nvgContext, x, y, w, h, cornerRadius);
        NanoVG.nvgPathWinding(nvgContext, NanoVG.NVG_HOLE);
        NanoVG.nvgFillPaint(nvgContext, paintA);
        NanoVG.nvgFill(nvgContext);

        // Header
        NanoVG.nvgLinearGradient(nvgContext, x, y, x, y + 15,
                rgba(255, 255, 255, 8, colorA),
                rgba(0, 0, 0, 16, colorA), paintB);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgRoundedRect(nvgContext, x + 1, y + 1, w - 2, 30, cornerRadius - 1);
        NanoVG.nvgFillPaint(nvgContext, paintB);
        NanoVG.nvgFill(nvgContext);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgMoveTo(nvgContext, x + 0.5f, y + 0.5f + 30);
        NanoVG.nvgLineTo(nvgContext, x + 0.5f + w - 1, y + 0.5f + 30);
        NanoVG.nvgStrokeColor(nvgContext, rgba(0, 0, 0, 32, colorA));
        NanoVG.nvgStroke(nvgContext);

        NanoVG.nvgFontSize(nvgContext, 15.0f);
        NanoVG.nvgFontFace(nvgContext, fontName);
        NanoVG.nvgTextAlign(nvgContext, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

        NanoVG.nvgFontBlur(nvgContext, 2);
        NanoVG.nvgFillColor(nvgContext, rgba(0, 0, 0, 128, colorA));
        NanoVG.nvgText(nvgContext, x + w / 2, y + 16 + 1, title);

        NanoVG.nvgFontBlur(nvgContext, 0);
        NanoVG.nvgFillColor(nvgContext, rgba(220, 220, 220, 160, colorA));
        NanoVG.nvgText(nvgContext, x + w / 2, y + 16, title);

        NanoVG.nvgRestore(nvgContext);
    }

    private void drawColorwheel(Arena arena, float x, float y, float w, float h, float t) {
        int i;
        float r0, r1, ax, ay, bx, by, cx, cy, aeps, r;
        float hue = (float) Math.sin(t * 0.12f);

        NanoVG.nvgSave(nvgContext);

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
            float a0 = (float) i / 6.0f * NanoVG.NVG_PI * 2.0f - aeps;
            float a1 = (float) (i + 1.0f) / 6.0f * NanoVG.NVG_PI * 2.0f + aeps;
            NanoVG.nvgBeginPath(nvgContext);
            NanoVG.nvgArc(nvgContext, cx, cy, r0, a0, a1, NanoVG.NVG_CW);
            NanoVG.nvgArc(nvgContext, cx, cy, r1, a1, a0, NanoVG.NVG_CCW);
            NanoVG.nvgClosePath(nvgContext);
            ax = cx + (float) Math.cos(a0) * (r0 + r1) * 0.5f;
            ay = cy + (float) Math.sin(a0) * (r0 + r1) * 0.5f;
            bx = cx + (float) Math.cos(a1) * (r0 + r1) * 0.5f;
            by = cy + (float) Math.sin(a1) * (r0 + r1) * 0.5f;
            NanoVG.nvgLinearGradient(nvgContext, ax, ay, bx, by,
                    NanoVG.nvgHSLA(a0 / (NanoVG.NVG_PI * 2f), 1.0f, 0.55f, (byte) 255, colorA),
                    NanoVG.nvgHSLA(a1 / (NanoVG.NVG_PI * 2f), 1.0f, 0.55f, (byte) 255, colorA),
                    paintB);
            NanoVG.nvgFillPaint(nvgContext, paintB);
            NanoVG.nvgFill(nvgContext);
        }

        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgCircle(nvgContext, cx, cy, r0 - 0.5f);
        NanoVG.nvgCircle(nvgContext, cx, cy, r1 + 0.5f);
        NanoVG.nvgStrokeColor(nvgContext, rgba(0, 0, 0, 64, colorA));
        NanoVG.nvgStrokeWidth(nvgContext, 1.0f);
        NanoVG.nvgStroke(nvgContext);

        // Selector
        NanoVG.nvgSave(nvgContext);
        NanoVG.nvgTranslate(nvgContext, cx, cy);
        NanoVG.nvgRotate(nvgContext, hue * NanoVG.NVG_PI * 2);

        // Marker on
        NanoVG.nvgStrokeWidth(nvgContext, 2.0f);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgRect(nvgContext, r0 - 1, -3, r1 - r0 + 2, 6);
        NanoVG.nvgStrokeColor(nvgContext, rgba(255, 255, 255, 192, colorA));
        NanoVG.nvgStroke(nvgContext);

        NanoVG.nvgBoxGradient(nvgContext, r0 - 3, -5, r1 - r0 + 6, 10, 2, 4,
                rgba(0, 0, 0, 128, colorA),
                rgba(0, 0, 0, 0, colorB), paintB);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgRect(nvgContext, r0 - 2 - 10, -4 - 10, r1 - r0 + 4 + 20, 8 + 20);
        NanoVG.nvgRect(nvgContext, r0 - 2, -4, r1 - r0 + 4, 8);
        NanoVG.nvgPathWinding(nvgContext, NanoVG.NVG_HOLE);
        NanoVG.nvgFillPaint(nvgContext, paintB);
        NanoVG.nvgFill(nvgContext);

        // Center triangle
        r = r0 - 6;
        ax = (float) Math.cos(120.0f / 180.0f * NanoVG.NVG_PI) * r;
        ay = (float) Math.sin(120.0f / 180.0f * NanoVG.NVG_PI) * r;
        bx = (float) Math.cos(-120.0f / 180.0f * NanoVG.NVG_PI) * r;
        by = (float) Math.sin(-120.0f / 180.0f * NanoVG.NVG_PI) * r;
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgMoveTo(nvgContext, r, 0);
        NanoVG.nvgLineTo(nvgContext, ax, ay);
        NanoVG.nvgLineTo(nvgContext, bx, by);
        NanoVG.nvgClosePath(nvgContext);
        NanoVG.nvgLinearGradient(nvgContext, r, 0, ax, ay,
                NanoVG.nvgHSLA(hue, 1.0f, 0.5f, (byte) 255, colorA),
                rgba(255, 255, 255, 255, colorB), paintB);
        NanoVG.nvgFillPaint(nvgContext, paintB);
        NanoVG.nvgFill(nvgContext);
        NanoVG.nvgLinearGradient(nvgContext, (r + ax) * 0.5f, (0 + ay) * 0.5f, bx, by,
                rgba(0, 0, 0, 0, colorA),
                rgba(0, 0, 0, 255, colorB), paintB);
        NanoVG.nvgFillPaint(nvgContext, paintB);
        NanoVG.nvgFill(nvgContext);
        NanoVG.nvgStrokeColor(nvgContext, rgba(0, 0, 0, 64, colorA));
        NanoVG.nvgStroke(nvgContext);

        // Select circle on triangle
        ax = (float) Math.cos(120.0f / 180.0f * NanoVG.NVG_PI) * r * 0.3f;
        ay = (float) Math.sin(120.0f / 180.0f * NanoVG.NVG_PI) * r * 0.4f;
        NanoVG.nvgStrokeWidth(nvgContext, 2.0f);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgCircle(nvgContext, ax, ay, 5);
        NanoVG.nvgStrokeColor(nvgContext, rgba(255, 255, 255, 192, colorA));
        NanoVG.nvgStroke(nvgContext);

        NanoVG.nvgRadialGradient(nvgContext, ax, ay, 7, 9,
                rgba(0, 0, 0, 64, colorA),
                rgba(0, 0, 0, 0, colorA), paintB);
        NanoVG.nvgBeginPath(nvgContext);
        NanoVG.nvgRect(nvgContext, ax - 20, ay - 20, 40, 40);
        NanoVG.nvgCircle(nvgContext, ax, ay, 7);
        NanoVG.nvgPathWinding(nvgContext, NanoVG.NVG_HOLE);
        NanoVG.nvgFillPaint(nvgContext, paintB);
        NanoVG.nvgFill(nvgContext);
        NanoVG.nvgRestore(nvgContext);
        NanoVG.nvgRestore(nvgContext);
    }

    static NVGColor rgba(int r, int g, int b, int a, NVGColor color) {
        color.r(r / 255.0f);
        color.g(g / 255.0f);
        color.b(b / 255.0f);
        color.a(a / 255.0f);

        return color;
    }
}
