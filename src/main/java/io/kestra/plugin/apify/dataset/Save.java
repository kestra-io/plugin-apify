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
    title = "Save Apify Dataset to File",
    description = "This task uses short polling to save the dataset from Apify to a temp file. " +
        "If this task receives an empty dataset, it will retry with exponential back-off until the dataset becomes " +
        "available or the timeout limit is reached. By default, the task will time out after 300 seconds to prevent " +
        "it from hanging. When this task receives a empty dataset it is typically " +
        "because the actor run has not finished uploading the Dataset."
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
        title = "format",
        description = "The format of the dataset. Defaults to `JSON`."
    )
    @Builder.Default
    private Property<DataSetFormat> format = Property.ofValue(DataSetFormat.JSON);

    @Schema(
        title = "delimiter",
        description = "A delimiter character for CSV files, only used if format=csv."
    )
    @Builder.Default
    private Property<String> delimiter = Property.ofValue(",");

    @Schema(
        title = "bom",
        description = "All text responses are encoded in UTF-8 encoding. By default, the format=csv files are " +
            "prefixed with the UTF-8 Byte Order Mark (BOM), while json, jsonl, xml, html and rss files are not." +
            "If you want to override this default behavior, specify bom=1 to include the BOM or bom=0 " +
            "to skip it. By default this value is not included in request made to the Apify API."
    )
    private Property<Boolean> bom;

    @Schema(
        title = "xmlRoot",
        description = "Overrides default root element name of xml output. By default the root element is items."
    )
    @Builder.Default
    private Property<String> xmlRoot = Property.ofValue("items");

    @Schema(
        title = "xmlRow",
        description = "Overrides default element name that wraps each page or page function result object in xml output. By default the element name is item."
    )
    @Builder.Default
    private Property<String> xmlRow = Property.ofValue("item");

    @Schema(
        title = "skipHeaderRow",
        description = "If true then header row in the csv format is skipped. Default value false."
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
            "format", runContext.render(this.format).as(DataSetFormat.class).orElseThrow(),
            "delimiter", runContext.render(this.delimiter).as(String.class).orElseThrow(),
            "xmlRoot", runContext.render(this.xmlRoot).as(String.class).orElseThrow(),
            "xmlRow", runContext.render(this.xmlRow).as(String.class).orElseThrow(),
            "skipHeaderRow", runContext.render(this.skipHeaderRow).as(Boolean.class).orElseThrow()
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
