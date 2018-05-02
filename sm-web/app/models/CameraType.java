package models;

public enum CameraType {
    RTMP("RTMP"),
    OPENCV("OPENCV"),
    STATIC("STATIC");

    private final String type;


    CameraType(final String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return type;
    }
}