package io.kestra.plugin.apify.task;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.Storage;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@KestraTest
class SaveDatasetToFileTest {
    @Inject
    RunContextFactory runContextFactory;

    @Test
    void testTimeoutBehavior() throws Exception {
        SaveDatasetToFile saveDatasetToFile = SaveDatasetToFile.builder()
            .datasetId(Property.ofValue("dataset-id"))
            .DEFAULT_TIMEOUT_DURATION(Duration.ofMillis(500))
            .build();

        SaveDatasetToFile saveDatasetToFileSpy = Mockito.spy(saveDatasetToFile);
        RunContext runContext = Mockito.spy(runContextFactory.of());

        Storage storage = mock(Storage.class);

        when(storage.getFile(any())).thenReturn(
            new ByteArrayInputStream("[]".getBytes())
        );

        when(runContext.storage()).thenReturn(storage);

        Mockito.doReturn(URI.create("kestra://fake-uri"))
            .when(saveDatasetToFileSpy)
            .makeCallAndWriteToFile(eq(runContext), any());

        Throwable timeoutException = assertThrows(IllegalStateException.class, () -> saveDatasetToFileSpy.run(runContext));
        assertEquals(
            "Timeout reached before dataset was available, please try again later or increase the timeout duration of the task.",
            timeoutException.getMessage()
        );
    }
}