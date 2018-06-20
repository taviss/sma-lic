package com.sma.core.camera.api;

/**
 * A video camera capable of returning snapshots(images) as byte arrays
 */
public interface Camera {
    /**
     * Get the snapshot
     * @return a byte array image
     */
    byte[] getSnapshot();

    /**
     * The camera's ID
     * @return the id
     */
    String getId();
}
