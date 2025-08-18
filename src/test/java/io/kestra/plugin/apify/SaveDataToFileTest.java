package io.kestra.plugin.apify;

import io.kestra.core.models.property.Property;
import io.kestra.plugin.apify.task.SaveDataToFile;
import io.kestra.plugin.apify.task.GetDataset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SaveDataToFileTest extends AbstractTest {
    @Test
    void givenOnlyRequiredValuesAreProvided_wheBuildingTheUrl_thenDefaultValueShouldBeSetWhereApplicable() throws Exception {
        SaveDataToFile getStructuredDataset = SaveDataToFile.builder()
            .id("TASK_ID")
            .type(GetDataset.class.getName())
            .datasetId(Property.ofValue("DATASET_ID"))
            .apiToken(Property.ofValue("API_KEY"))
            .build();

        // Assert that optional value with default values are set
        String uri = getStructuredDataset.buildURL(runContext(getStructuredDataset));
        assertThat(uri,
            equalTo("/datasets/DATASET_ID/items?flatten=false&sortDirection=false&offset=0&simplified=false" +
                "&skipEmpty=true&limit=1000&skipHidden=false&datasetId=DATASET_ID&skipFailedPages=false&cleanValue=true" +
                "&xmlRow=item&delimiter=%2C&format=json&skipHeaderRow=false&xmlRoot=items"
            )
        );
    }

    @Test
    void givenAllValuesAreProvided_wheBuildingTheUrl_thenAllValuesShouldBePopulated() throws Exception {
        SaveDataToFile getStructuredDataset = SaveDataToFile.builder()
            .id("TASK_ID")
            .type(GetDataset.class.getName())
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

        String uri = getStructuredDataset.buildURL(runContext(getStructuredDataset));
        // Assert that all values are set
        assertThat(uri,
            equalTo("/datasets/DATASET_ID/items?offset=1&skipEmpty=true&skipHidden=true&unwind=postMeta&flatten=true" +
                "&sortDirection=false&view=DUMMY_VIEW_VALUE&simplified=true&limit=10&datasetId=DATASET_ID" +
                "&fields=userId%2C%23id%2C%23createdAt%2CpostMeta&omit=%23id&skipFailedPages=true&cleanValue=true" +
                "&bom=true&xmlRow=xmlRow&delimiter=%2C+&format=csv&skipHeaderRow=true&xmlRoot=xmlRoot"
            )
        );
    }
}