package com.fabcc.nanovg.example;

import java.util.Arrays;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class FrameLimiter {

    private final int fps;
    private int frameIndex;
    private double frameTime;
    private long[] nvgTimeNs;
    private LongList nvgTimeNsList;
    private FloatList frameTimeList;

    transient long nvgStartNs;
    transient long nvgEndNs;

    public FrameLimiter(int fps) {
        this.fps = fps;
        this.frameTime = 1.0 / fps;
        this.nvgTimeNs = new long[fps];
        this.nvgTimeNsList = new LongArrayList();
        this.frameTimeList = new FloatArrayList();
    }

    public void startNvg() {
        nvgStartNs = System.nanoTime();
    }

    public void endNvg() {
        long nvgEndNs = System.nanoTime();
        nvgTimeNsList.add(nvgEndNs - nvgStartNs);
        nvgTimeNs[frameIndex] = (nvgEndNs - nvgStartNs);
    }

    /**
     * 
     * @param deltaTime Delta time in second
     */
    public void frameLimit(double deltaTime) {
        if (deltaTime < frameTime) {
            // Print p50, p90, p99 NVG times
            if (frameIndex >= fps - 1) {
                Arrays.sort(nvgTimeNs);
                long p50 = nvgTimeNs[(int) (fps * 0.5)];
                long p90 = nvgTimeNs[(int) (fps * 0.9)];
                long p99 = nvgTimeNs[(int) (fps * 0.99)];
                System.out.println(String.format("NVG Times (ms): p50=%.3f, p90=%.3f, p99=%.3f",
                        p50 / 1_000_000.0,
                        p90 / 1_000_000.0,
                        p99 / 1_000_000.0));
                frameIndex = 0;
            } else {
                frameIndex++;
            }
            this.frameTimeList.add((float) deltaTime);
            try {
                Thread.sleep((long) ((frameTime - deltaTime) * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void printResults() {
        long[] sortedTimes = nvgTimeNsList.toLongArray();
        Arrays.sort(sortedTimes);
        long p50 = sortedTimes[(int) (sortedTimes.length * 0.5)];
        long p90 = sortedTimes[(int) (sortedTimes.length * 0.9)];
        long p95 = sortedTimes[(int) (sortedTimes.length * 0.95)];
        long p99 = sortedTimes[(int) (sortedTimes.length * 0.99)];
        System.out.println("Final NVG Times (ms):");
        System.out.println(String.format("p50=%.3f, p90=%.3f, p95=%.3f, p99=%.3f",
                p50 / 1_000_000.0,
                p90 / 1_000_000.0,
                p95 / 1_000_000.0,
                p99 / 1_000_000.0));

        float[] sortedFrameTimes = frameTimeList.toFloatArray();
        Arrays.sort(sortedFrameTimes);
        float f_p50 = sortedFrameTimes[(int) (sortedFrameTimes.length * 0.5)];
        float f_p90 = sortedFrameTimes[(int) (sortedFrameTimes.length * 0.9)];
        float f_p95 = sortedFrameTimes[(int) (sortedFrameTimes.length * 0.95)];
        float f_p99 = sortedFrameTimes[(int) (sortedFrameTimes.length * 0.99)];
        System.out.println("Final Frame Times (ms):");
        System.out.println(String.format("p50=%.3f, p90=%.3f, p95=%.3f, p99=%.3f",
                f_p50 * 1000,
                f_p90 * 1000,
                f_p95 * 1000,
                f_p99 * 1000));
        
    }

}
