package com.fabcc.nanovg.example;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySegment.Scope;

/**
 * A custom allocator that traces memory allocations.
 */
public class TracedArena implements Arena {

    final Arena delegate = Arena.ofConfined();
    long allocatedBytes = 0;

    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        MemorySegment segment = delegate.allocate(byteSize, byteAlignment);
        allocatedBytes += byteSize;
        return segment;
    }

    @Override
    public Scope scope() {
        return delegate.scope();
    }

}
