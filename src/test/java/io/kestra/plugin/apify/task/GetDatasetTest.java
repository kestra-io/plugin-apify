package io.kestra.plugin.apify.task;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@KestraTest
class GetDatasetTest {
    @Inject
    RunContextFactory runContextFactory = new RunContextFactory();

    @Test
    void testTimeoutBehavior() throws Exception {
        GetDataset getDataset = GetDataset.builder()
            .datasetId(Property.ofValue("dataset-id"))
            .DEFAULT_TIMEOUT_DURATION(Duration.ofMillis(500))
            .build();

        GetDataset getDatasetSpy = Mockito.spy(getDataset);
        RunContext runContext = runContextFactory.of();

        Mockito.doReturn(Collections.emptyList())
            .when(getDatasetSpy)
            .makeCall(eq(runContext), any(), eq(List.class));

        Throwable timeoutException = assertThrows(IllegalStateException.class, () -> getDatasetSpy.run(runContext));
        assertEquals(
            "Timeout reached before dataset was available, please try again later or increase the timeout duration of the task.",
            timeoutException.getMessage()
        );
    }
}