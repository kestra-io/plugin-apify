package io.kestra.plugin.apify.dataset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveDeprecationTest {

    @Test
    void givenSaveClass_whenCheckingDeprecation_thenIsAnnotatedAsDeprecated() {
        assertTrue(Save.class.isAnnotationPresent(Deprecated.class));
    }
}
