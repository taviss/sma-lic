package com.sma.core.boject.finder.service.impl;

import com.sma.core.camera.api.Camera;

public class MockCamera implements Camera {
    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public String getId() {
        return null;
    }
}
