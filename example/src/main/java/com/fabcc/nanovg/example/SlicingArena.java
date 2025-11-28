package com.fabcc.nanovg.example;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySegment.Scope;

public class SlicingArena implements Arena {

    private final MemorySegment baseSegment;
    private final long size;
    private SlicingAllocator slicingAllocator;

    public SlicingArena(MemorySegment baseSegment, long size) {
        this.baseSegment = baseSegment;
        this.size = size;
        this.slicingAllocator = new SlicingAllocator(baseSegment);
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        if (byteSize > size) {
            throw new OutOfMemoryError("Requested size exceeds arena size");
        }
        return slicingAllocator.allocate(byteSize, byteAlignment);
    }

    public void reset() {
        this.slicingAllocator.resetTo(0);
    }

    @Override
    public void close() {
        baseSegment.fill((byte) 0);
    }

    @Override
    public Scope scope() {
        return baseSegment.scope();
    }

}
