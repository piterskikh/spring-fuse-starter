package com.example.fuse;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fuse")
public class FuseProperties {
    private long windowMillis = 100;

    public long getWindowMillis() {
        return windowMillis;
    }

    public void setWindowMillis(long windowMillis) {
        this.windowMillis = windowMillis;
    }
}
