package io.kestra.plugin.apify.task;

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
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Save Apify Dataset to File",
    description = "This task uses short polling to save the dataset from Apify to a temp file. " +
        "If this tasks received an empty dataset, it will retry the request every 5 seconds until the dataset is " +
        "available or the tasks timeout limit is reached. When this task receives a empty dataset it is typically " +
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
                       type: io.kestra.plugin.apify.task.SaveDatasetToFile
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
                       type: io.kestra.plugin.apify.task.SaveDatasetToFile
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
public class SaveDatasetToFile extends AbstractGetDataset implements RunnableTask<SaveDatasetToFile.Output> {
    private static final Logger log = LoggerFactory.getLogger(SaveDatasetToFile.class);
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
        Logger logger = runContext.logger();
        String url = this.buildURL(runContext);

        /*
         * It can take several seconds between an actor run finishing and a dataset being fully uploaded.
         * If the user uses both the ActorRun and SaveDatasetToFile task,
         * we need to retry the request if we get an empty response.
         */
        Instant end = Instant.now().plus(DEFAULT_TIMEOUT_DURATION);
        boolean isTaskTimeoutSet = runContext.render(this.timeout).as(Duration.class).isPresent();
        Instant doNextCallAt = Instant.now();
        int retryCount = 0;

        while (!isTaskTimeoutSet && end.isAfter(Instant.now())) {
            if (doNextCallAt.isBefore(Instant.now())) {
                URI uri = this.makeCallAndWriteToFile(runContext, this.buildGetRequest(url));

                try (InputStream inputStream = runContext.storage().getFile(uri)) {
                    byte[] firstTwoChars = inputStream.readNBytes(2);
                    if (!Arrays.equals(firstTwoChars, EMPTY_DATASET_BYTES)) {
                        return new Output(uri);
                    }
                }

                retryCount++;
                int retryDelay = (int) (Math.pow(2, retryCount) * 1000);
                logger.debug("Received empty dataset, will retry again in {}ms", retryDelay);
                doNextCallAt = Instant.now().plus(Duration.ofMillis(retryDelay));
            }
        }

        throw new IllegalStateException("Timeout reached before dataset was available, please try again later or increase the timeout duration of the task.");
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
}
