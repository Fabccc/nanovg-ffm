package com.fabcc.nanovg.example.lwjgl;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.lang.foreign.Arena;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.fabcc.nanovg.example.FrameLimiter;
import com.fabcc.nanovg.lib.NVG;
import com.fabcc.nanovg.lib.NVGLoader;

public class MainLwjgl {
    // The window handle
    private long window;
    private boolean isWayland;
    private Arena arena;
    private float windowWidth;
    private float windowHeight;
    private long nvgContext;
    private NVGDemoLWJGL demo;

    public MainLwjgl(Arena arena) {
        this.arena = arena;
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        this.isWayland = waylandDisplay != null && !waylandDisplay.isBlank();
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        NVGLoader.init();

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        if (!this.isWayland) {
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated
        // or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetFramebufferSizeCallback(window, (winId, w, h) -> {
            this.windowHeight = h;
            this.windowWidth = w;
            glViewport(0, 0, w, h);
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (!this.isWayland) {
                // Can center only on wayland
                // Center the window
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2);

            }
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GLFW.glfwSetTime(0);
        // GLFW.glfwSetInputMode(windowId, GLFW.GLFW_CURSOR(),
        // GLFW.GLFW_CURSOR_HIDDEN());
        GL.createCapabilities();
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glEnable(GL30.GL_BLEND);

        this.nvgContext = NanoVGGL3.nvgCreate(
                NanoVGGL3.NVG_STENCIL_STROKES | NanoVGGL3.NVG_DEBUG);
        if (this.nvgContext == 0) {
            throw new IllegalStateException("Can't create nvgContext");
        }
        this.demo = new NVGDemoLWJGL(nvgContext, "NanoVG LWJGL");
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        double previousTime = GLFW.glfwGetTime();

        // Set the clear color
        glClearColor(0.1f, 0.1f, 0.3f, 1.0f);

        FrameLimiter frameLimiter = new FrameLimiter(140);
        
        try (MemoryStack stack = stackPush()) {
            DoubleBuffer xBuffer = stack.mallocDouble(1);
            DoubleBuffer yBuffer = stack.mallocDouble(1);
            // Run the rendering loop until the user has attempted to close
            // the window or has pressed the ESCAPE key.
            while (!glfwWindowShouldClose(window)) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double cx = xBuffer.get(0);
                double cy = yBuffer.get(0);
                double time = glfwGetTime();
                double deltaTime = time - previousTime;
                previousTime = time;

                frameLimiter.startNvg();
                NanoVG.nvgBeginFrame(nvgContext, this.windowWidth, this.windowHeight, 1.0f);
                demo.renderDemo(cx, cy, this.windowWidth, this.windowHeight, time);
                NanoVG.nvgEndFrame(nvgContext);
                frameLimiter.endNvg();
                frameLimiter.frameLimit(deltaTime);

                glfwSwapBuffers(window); // swap the color buffers

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();
            }
        }
        // Print p50, p90, p95, p99 NVG times
        frameLimiter.printResults();
    }

    public static void main(String[] args) {
        try (Arena arena = Arena.ofConfined()) {
            new MainLwjgl(arena).run();
        }
    }
}
