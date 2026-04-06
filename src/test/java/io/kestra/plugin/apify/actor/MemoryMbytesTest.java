package io.kestra.plugin.apify.actor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoryMbytesTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void givenMemoryMbytesEnum_whenCountingValues_thenHasNineConstants() {
        assertEquals(9, MemoryMbytes.values().length);
    }

    @Test
    void givenMemoryMbytesConstants_whenGettingValue_thenReturnsExpectedInt() {
        assertEquals(128, MemoryMbytes.MB_128.getValue());
        assertEquals(32768, MemoryMbytes.MB_32768.getValue());
    }

    @Test
    void givenMemoryMbytesEnum_whenSerializingWithJackson_thenProducesExpectedInteger() throws Exception {
        assertEquals("128", mapper.writeValueAsString(MemoryMbytes.MB_128));
        assertEquals("256", mapper.writeValueAsString(MemoryMbytes.MB_256));
        assertEquals("1024", mapper.writeValueAsString(MemoryMbytes.MB_1024));
        assertEquals("32768", mapper.writeValueAsString(MemoryMbytes.MB_32768));
    }
}
