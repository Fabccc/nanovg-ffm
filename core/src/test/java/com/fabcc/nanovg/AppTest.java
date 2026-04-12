package com.fabcc.nanovg;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.fabcc.nanovg.lib.NVGLoader;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void loadLibrary() {
        // The library should be loaded
        assertDoesNotThrow(() -> {
            NVGLoader.init();
            NVGLoader.cleanup();
        });
    }
}
