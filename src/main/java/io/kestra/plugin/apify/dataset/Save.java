package io.kestra.plugin.apify.dataset;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.DataSetFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Save Apify dataset to temp file",
    description = "Downloads dataset items to Kestra temp storage with short polling and exponential backoff until data appears or the 300s timeout is reached. Retries on empty responses while the actor finishes writing."
)
@Plugin(
    examples = {
        @Example(
            title = "Save dataset with given id as temp file.",
            full = true,
            code = """
                   id: apify_save_dataset_flow_required_properties
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.dataset.Save
                       apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                       datasetId: mecGriFjtDHRNtYOZ
                   """
        ),
        @Example(
            title = "Save dataset with given id as temp file.",
            full = true,
            code = """
                   id: save_data_set_to_csv_file
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.dataset.Save
                       apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                       datasetId: RNtYOZmecGriFjtDH
                       format: CSV
                       fields: userId, #id, #createdAt, postMeta
                       omit: #id
                       flatten: postMeta
                       sort: ASC
                   """
        )
    }
)
public class Save extends AbstractGetDataset implements RunnableTask<Save.Output> {
    private static final Logger log = LoggerFactory.getLogger(Save.class);
    @Schema(
        title = "Format",
        description = "Dataset export format; defaults to JSON."
    )
    @Builder.Default
    private Property<DataSetFormat> format = Property.ofValue(DataSetFormat.JSON);

    @Schema(
        title = "Delimiter",
        description = "CSV delimiter when format is CSV; ignored otherwise."
    )
    @Builder.Default
    private Property<String> delimiter = Property.ofValue(",");

    @Schema(
        title = "Include BOM",
        description = "Force UTF-8 BOM for text outputs; Apify adds it to CSV by default and omits for JSON/JSONL/XML/HTML/RSS unless overridden."
    )
    private Property<Boolean> bom;

    @Schema(
        title = "XML root",
        description = "Root element name for XML output; defaults to items."
    )
    @Builder.Default
    private Property<String> xmlRoot = Property.ofValue("items");

    @Schema(
        title = "XML row",
        description = "Element name for each record in XML output; defaults to item."
    )
    @Builder.Default
    private Property<String> xmlRow = Property.ofValue("item");

    @Schema(
        title = "Skip header row",
        description = "Omit CSV header row when true; default false."
    )
    @Builder.Default
    private Property<Boolean> skipHeaderRow = Property.ofValue(false);

    private static final byte[] EMPTY_DATASET_BYTES = "[]".getBytes();

    @Override
    public Output run(RunContext runContext) throws Exception {
        String url = this.buildURL(runContext);

        URI uri = withRetry(
            runContext,
            isEmptyDataset(runContext),
            () -> this.makeCallAndWriteToFile(runContext, this.buildGetRequest(url))
        );

        return new Output(uri);
    }

    @Override
    public String buildURL(RunContext runContext) throws IllegalVariableEvaluationException {
        Optional<Boolean> rBom = runContext.render(this.bom).as(Boolean.class);

        String baseUrl = super.buildURL(runContext);
        final Map<String, Object> queryParamValues = new HashMap<>(Map.of(
            "format", runContext.render(this.format).as(DataSetFormat.class).orElse(DataSetFormat.JSON),
            "delimiter", runContext.render(this.delimiter).as(String.class).orElse(","),
            "xmlRoot", runContext.render(this.xmlRoot).as(String.class).orElse("items"),
            "xmlRow", runContext.render(this.xmlRow).as(String.class).orElse("item"),
            "skipHeaderRow", runContext.render(this.skipHeaderRow).as(Boolean.class).orElse(false)
        ));

        rBom.ifPresent(b -> queryParamValues.put("bom", b));

        return addQueryParams(baseUrl, queryParamValues);
    }

    @Getter
    @AllArgsConstructor
    public static class Output implements io.kestra.core.models.tasks.Output {
        private URI path;
    }

    private static Predicate<URI> isEmptyDataset(RunContext runContext) {
        return (URI uri) -> {
            try (InputStream inputStream = runContext.storage().getFile(uri)) {
                byte[] firstTwoChars = inputStream.readNBytes(2);
                return Arrays.equals(firstTwoChars, EMPTY_DATASET_BYTES);
            } catch (Exception e) {
                log.error("Failed to read dataset file", e);
                return false;
            }
        };
    }
}
