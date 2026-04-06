package io.kestra.plugin.apify;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.media.Schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApifyConnectionInterfaceTest {

    @Test
    void givenApiTokenMethod_whenInspectingSchemaAnnotation_thenFormatIsPassword() throws NoSuchMethodException {
        var method = ApifyConnectionInterface.class.getMethod("getApiToken");
        var schema = method.getAnnotation(Schema.class);
        assertEquals("password", schema.format());
    }
}
