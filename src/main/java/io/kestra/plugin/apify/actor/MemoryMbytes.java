package io.kestra.plugin.apify.actor;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MemoryMbytes {
    MB_128(128),
    MB_256(256),
    MB_512(512),
    MB_1024(1024),
    MB_2048(2048),
    MB_4096(4096),
    MB_8192(8192),
    MB_16384(16384),
    MB_32768(32768);

    private final int value;

    MemoryMbytes(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
