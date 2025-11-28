package com.fabcc.nanovg.example.glfwffm;

import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.MemorySegment;

import com.fabcc.nanovg.example.FrameLimiter;
import com.fabcc.nanovg.example.NVGDemo;
import com.fabcc.nanovg.example.SlicingArena;
import com.fabcc.nanovg.example.TracedArena;
import com.fabcc.nanovg.example.glfwffm.lib.GL;
import com.fabcc.nanovg.example.glfwffm.lib.GLFW;
import com.fabcc.nanovg.example.glfwffm.lib.GLFWerrorfun;
import com.fabcc.nanovg.example.glfwffm.lib.GLFWframebuffersizefun;
import com.fabcc.nanovg.lib.NVG;
import com.fabcc.nanovg.lib.NVGLoader;

public class Main {

    public static final boolean VSYNC = true;

    static int fbWidth;
    static int fbHeight;

    public static void main(String[] args) {
        try (TracedArena arena = new TracedArena()) {
            NVGLoader.init();
            // Initialize GLFW and create a window here
            GL.glewInit();
            GLFW.glfwInit();
            var windowTitle = arena.allocateFrom("NanoVG FFM");
            var windowId = GLFW.glfwCreateWindow(
                    800, 600, windowTitle, MemorySegment.NULL,
                    MemorySegment.NULL);

            GLFW.glfwSetErrorCallback(GLFWerrorfun.allocate((error, description) -> {
                System.err.println("GLFW Error " + error + ": " + description.getString(0));
            }, arena));
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR(), 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR(), 2);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT(), GL.GL_TRUE());
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE(), GLFW.GLFW_OPENGL_CORE_PROFILE());

            MemorySegment widthPtr = arena.allocate(JAVA_INT);
            MemorySegment heightPtr = arena.allocate(JAVA_INT);
            GLFW.glfwGetFramebufferSize(windowId, widthPtr, heightPtr);
            fbWidth = widthPtr.get(JAVA_INT, 0);
            fbHeight = heightPtr.get(JAVA_INT, 0);

            GL.glViewport(0, 0, fbWidth, fbHeight);

            GLFW.glfwSetFramebufferSizeCallback(windowId,
                    GLFWframebuffersizefun.allocate((_, width, height) -> {
                        System.out.println("Framebuffer resized to " + width + "x" + height);
                        GL.glViewport(0, 0, width, height);
                        fbWidth = width;
                        fbHeight = height;
                    }, arena));
            GLFW.glfwMakeContextCurrent(windowId);
            if (!VSYNC) {
                GLFW.glfwSwapInterval(0);
            }
            GL.glClearColor(0.1f, 0.1f, 0.3f, 1.0f);
            var nvgContext = NVG.nvgCreateContext(NVG.NVG_STENCIL_STROKES() | NVG.NVG_DEBUG());
            if (nvgContext.equals(MemorySegment.NULL)) {
                throw new RuntimeException("Could not init NanoVG context.");
            }
            GLFW.glfwSetTime(0);
            // GLFW.glfwSetInputMode(windowId, GLFW.GLFW_CURSOR(),
            // GLFW.GLFW_CURSOR_HIDDEN());
            double previousTime = GLFW.glfwGetTime();

            FrameLimiter frameLimiter = new FrameLimiter(140);
            long allocatedAtStart = arena.getAllocatedBytes();
            long previousAllocated = allocatedAtStart;

            MemorySegment cxPtr = arena.allocate(JAVA_DOUBLE);
            MemorySegment cyPtr = arena.allocate(JAVA_DOUBLE);
            double cx = 0;
            double cy = 0;

            // Allocate 4kB for the slicing arena
            long size = 4 * 1024;
            MemorySegment slicingArenaBase = arena.allocate(size, 8);
            SlicingArena slicingArena = new SlicingArena(slicingArenaBase, size);
            var demo = new NVGDemo(slicingArena, "NanoVG FFM", nvgContext);

            while (GLFW.glfwWindowShouldClose(windowId) == GLFW.GLFW_FALSE()) {
                GL.glClear(GL.GL_COLOR_BUFFER_BIT() | GL.GL_DEPTH_BUFFER_BIT() | GL.GL_STENCIL_BUFFER_BIT());

                double time = GLFW.glfwGetTime();
                double deltaTime = time - previousTime;
                previousTime = time;

                long currentAllocated = arena.getAllocatedBytes();
                long deltaAllocated = currentAllocated - previousAllocated;
                previousAllocated = currentAllocated;

                GLFW.glfwGetCursorPos(windowId, cxPtr, cyPtr);
                cx = cxPtr.get(JAVA_DOUBLE, 0);
                cy = cyPtr.get(JAVA_DOUBLE, 0);

                float windowWidth = fbWidth;
                float windowHeight = fbHeight;

                frameLimiter.startNvg();
                NVG.nvgBeginFrame(nvgContext, windowWidth, windowHeight, 1.0f);
                demo.renderDemoLowMemalloc(slicingArena, cx, cy, windowWidth, windowHeight, time);
                slicingArena.reset();
                NVG.nvgEndFrame(nvgContext);
                frameLimiter.endNvg();
                frameLimiter.frameLimit(deltaTime);
                GLFW.glfwSwapBuffers(windowId);
                GLFW.glfwPollEvents();
            }
            NVG.nvgDeleteContext(nvgContext);
            NVGLoader.cleanup();
            GLFW.glfwTerminate();
            // Print p50, p90, p95, p99 NVG times
            frameLimiter.printResults();
            long allocatedAtEnd = arena.getAllocatedBytes();
            System.out.println("Total allocated memory: " + (allocatedAtEnd - allocatedAtStart) + " bytes");
        }

    }
}
