package io.kestra.plugin.apify.actor;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.Storage;
import io.kestra.plugin.apify.ApifySortDirection;
import io.kestra.plugin.apify.DataSetFormat;
import io.kestra.plugin.apify.dataset.Get;
import io.kestra.plugin.apify.dataset.Save;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@KestraTest
class SaveTest {
    @Inject
    RunContextFactory runContextFactory;

    private static final URI fakeUri = URI.create("kestra://fake-uri");

    @Test
    void givenNoDatasetAvailable_whenRunExceedsTimeout_thenThrowsIllegalStateException() throws Exception {
        Save saveDatasetToFile = Save.builder()
            .datasetId(Property.ofValue("dataset-id"))
            .DEFAULT_TIMEOUT_DURATION(Duration.ofMillis(2500))
            .DEFAULT_MAX_INTERVAL_DURATION(Duration.ofMillis(2500))
            .build();

        Save saveDatasetToFileSpy = Mockito.spy(saveDatasetToFile);
        RunContext runContext = Mockito.spy(runContextFactory.of());

        Storage storage = mock(Storage.class);

        when(storage.getFile(any())).thenAnswer(
            (path) -> new ByteArrayInputStream("[]".getBytes())
        );

        when(runContext.storage()).thenReturn(storage);

        Mockito.doReturn(fakeUri)
            .when(saveDatasetToFileSpy)
            .makeCallAndWriteToFile(eq(runContext), any());

        Throwable timeoutException = assertThrows(IllegalStateException.class, () -> saveDatasetToFileSpy.run(runContext));
        assertEquals(
            "Timeout reached before dataset was available, please try again later or increase the timeout duration of the task.",
            timeoutException.getMessage()
        );
    }

    @Test
    void givenDatasetAvailable_whenRun_thenReturnsExpectedDataset() throws Exception {
        Save saveDatasetToFile = Save.builder()
            .datasetId(Property.ofValue("dataset-id"))
            .DEFAULT_TIMEOUT_DURATION(Duration.ofMillis(2500))
            .DEFAULT_MAX_INTERVAL_DURATION(Duration.ofMillis(2500))
            .build();

        Save saveDatasetToFileSpy = Mockito.spy(saveDatasetToFile);
        RunContext runContext = Mockito.spy(runContextFactory.of());

        Storage storage = mock(Storage.class);
        when(storage.getFile(any())).thenAnswer(
            (path) -> new ByteArrayInputStream("[{'key': 'value'}]".getBytes())
        );

        when(runContext.storage()).thenReturn(storage);

        Mockito.doReturn(fakeUri)
            .when(saveDatasetToFileSpy)
            .makeCallAndWriteToFile(eq(runContext), any());

        assertEquals(
            fakeUri,
            saveDatasetToFileSpy.run(runContext).getPath()
        );
    }

    @Test
    void givenOnlyRequiredValuesAreProvided_wheBuildingTheUrl_thenDefaultValueShouldBeSetWhereApplicable() throws Exception {
        Save getStructuredDataset = Save.builder()
            .id("TASK_ID")
            .type(Get.class.getName())
            .datasetId(Property.ofValue("DATASET_ID"))
            .apiToken(Property.ofValue("API_KEY"))
            .build();

        // Assert that optional value with default values are set
        String uri = getStructuredDataset.buildURL(runContextFactory.of());
        assertThat(uri,
            equalTo("/datasets/DATASET_ID/items?cleanValue=true&flatten=false&limit=1000&offset=0" +
                "&simplified=false&skipEmpty=true&skipFailedPages=false&skipHidden=false&sortDirection=false" +
                "&delimiter=%2C&format=json&skipHeaderRow=false&xmlRoot=items&xmlRow=item"
            )
        );
    }

    @Test
    void givenAllValuesAreProvided_wheBuildingTheUrl_thenAllValuesShouldBePopulated() throws Exception {
        Save getStructuredDataset = Save.builder()
            .id("TASK_ID")
            .type(Get.class.getName())
            .datasetId(Property.ofValue("DATASET_ID"))
            .clean(Property.ofValue(true))
            .offset(Property.ofValue(1))
            .limit(Property.ofValue(10))
            .fields(Property.ofValue(List.of("userId", "#id", "#createdAt", "postMeta")))
            .omit(Property.ofValue(List.of("#id")))
            .unwind(Property.ofValue(List.of("postMeta")))
            .flatten(Property.ofValue(true))
            .sort(Property.ofValue(ApifySortDirection.ASC))
            .skipEmpty(Property.ofValue(true))
            .skipFailedPages(Property.ofValue(true))
            .view(Property.ofValue("DUMMY_VIEW_VALUE"))
            .skipHidden(Property.ofValue(true))
            .simplified(Property.ofValue(true))
            .apiToken(Property.ofValue("API_KEY"))
            .format(Property.ofValue(DataSetFormat.CSV))
            .delimiter(Property.ofValue(", "))
            .xmlRoot(Property.ofValue("xmlRoot"))
            .xmlRow(Property.ofValue("xmlRow"))
            .skipHeaderRow(Property.ofValue(true))
            .bom(Property.ofValue(true))
            .build();

        String uri = getStructuredDataset.buildURL(runContextFactory.of());
        // Assert that all values are set
        assertThat(uri,
            equalTo("/datasets/DATASET_ID/items?cleanValue=true&fields=userId%2C%23id%2C%23createdAt%2CpostMeta" +
                "&flatten=true&limit=10&offset=1&omit=%23id&simplified=true&skipEmpty=true&skipFailedPages=true" +
                "&skipHidden=true&sortDirection=false&unwind=postMeta&view=DUMMY_VIEW_VALUE&bom=true&delimiter=%2C+" +
                "&format=csv&skipHeaderRow=true&xmlRoot=xmlRoot&xmlRow=xmlRow"
            )
        );
    }
}