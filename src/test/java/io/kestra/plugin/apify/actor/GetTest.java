package io.kestra.plugin.apify.actor;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.apify.ApifySortDirection;
import io.kestra.plugin.apify.dataset.Get;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@KestraTest
class GetTest {
    @Inject
    RunContextFactory runContextFactory = new RunContextFactory();

    @Test
    void givenNoDatasetAvailable_whenRunExceedsTimeout_thenThrowsIllegalStateException() throws Exception {
        Get getDataset = Get.builder()
            .datasetId(Property.ofValue("dataset-id"))
            .DEFAULT_TIMEOUT_DURATION(Duration.ofMillis(2500))
            .DEFAULT_MAX_INTERVAL_DURATION(Duration.ofMillis(2500))
            .build();

        Get getDatasetSpy = Mockito.spy(getDataset);
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

    @Test
    void givenDatasetAvailable_whenRun_thenReturnsExpectedDataset() throws Exception {
        List<Map<String, Object>> expected = List.of(Map.of("key", "value"));
        Get getDataset = Get.builder()
            .datasetId(Property.ofValue("dataset-id"))
            .DEFAULT_TIMEOUT_DURATION(Duration.ofMillis(2500))
            .DEFAULT_MAX_INTERVAL_DURATION(Duration.ofMillis(2500))
            .build();

        Get getDatasetSpy = Mockito.spy(getDataset);
        RunContext runContext = runContextFactory.of();

        Mockito.doReturn(expected)
            .when(getDatasetSpy)
            .makeCall(eq(runContext), any(), eq(List.class));


        assertEquals(
            expected,
            getDatasetSpy.run(runContext).dataset()
        );
    }

    @Test
    void givenOnlyRequiredValuesAreProvided_wheBuildingTheUrl_thenDefaultValueShouldBeSetWhereApplicable() throws Exception {
        Get getStructuredDataset = Get.builder()
            .datasetId(Property.ofValue("DATASET_ID"))
            .apiToken(Property.ofValue("API_KEY"))
            .build();

        // Assert that optional value with default values are set
        String uri = getStructuredDataset.buildURL(runContextFactory.of());
        assertThat(uri,
            equalTo("/datasets/DATASET_ID/items?cleanValue=true&flatten=false&limit=1000&offset=0" +
                "&simplified=false&skipEmpty=true&skipFailedPages=false&skipHidden=false&sortDirection=false"
            )
        );
    }

    @Test
    void givenAllValuesAreProvided_wheBuildingTheUrl_thenAllValuesShouldBePopulated() throws Exception {
        Get getStructuredDataset = Get.builder()
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
            .build();

        String uri = getStructuredDataset.buildURL(runContextFactory.of());
        // Assert that all values are set
        assertThat(uri,
            equalTo("/datasets/DATASET_ID/items?cleanValue=true" +
                "&fields=userId%2C%23id%2C%23createdAt%2CpostMeta&flatten=true&limit=10&offset=1" +
                "&omit=%23id&simplified=true&skipEmpty=true&skipFailedPages=true&skipHidden=true&sortDirection=false" +
                "&unwind=postMeta&view=DUMMY_VIEW_VALUE"
            )
        );
    }
}