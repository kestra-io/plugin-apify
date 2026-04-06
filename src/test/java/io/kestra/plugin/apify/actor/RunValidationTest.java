package io.kestra.plugin.apify.actor;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that {@code waitForFinish} in {@link Run} declares the expected {@code @Min(0)} and {@code @Max(60)}
 * constraints as type-use annotations on the generic type parameter {@code Property<@Min(0) @Max(60) Integer>}.
 */
class RunValidationTest {

    @Test
    void givenWaitForFinishField_whenInspectingConstraints_thenMinIsZeroAndMaxIsSixty() throws NoSuchFieldException {
        Field field = Run.class.getDeclaredField("waitForFinish");

        // The field type is Property<@Min(0) @Max(60) Integer>; the annotation sits on the type argument.
        AnnotatedType annotatedType = field.getAnnotatedType();
        AnnotatedType[] typeArguments = ((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments();
        AnnotatedType integerTypeArgument = typeArguments[0];

        Min min = integerTypeArgument.getAnnotation(Min.class);
        Max max = integerTypeArgument.getAnnotation(Max.class);

        assertNotNull(min, "@Min annotation must be present on waitForFinish type argument");
        assertNotNull(max, "@Max annotation must be present on waitForFinish type argument");
        assertEquals(0, min.value(), "@Min value must be 0");
        assertEquals(60, max.value(), "@Max value must be 60");
    }
}
