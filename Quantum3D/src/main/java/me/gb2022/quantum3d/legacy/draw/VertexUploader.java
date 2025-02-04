package me.gb2022.quantum3d.legacy.draw;

import java.util.concurrent.atomic.AtomicInteger;

public interface VertexUploader {
    AtomicInteger UPLOAD_COUNTER = new AtomicInteger(0);

    static int getUploadedCount() {
        return UPLOAD_COUNTER.get();
    }

    static void resetUploadCount() {
        UPLOAD_COUNTER.set(0);
    }
}