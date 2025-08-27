package io.kestra.plugin.apify.task;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.ApifyConnection;
import io.kestra.plugin.apify.ApifySortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractGetDataset extends ApifyConnection {
    @Schema(
        title = "datasetId"
    )
    @NotNull
    private Property<String> datasetId;


    @Schema(
        title = "Clean",
        description = "If true then the task returns only non-empty items and skips hidden fields (i.e. fields starting with the # character). The default value is true."
    )
    @Builder.Default
    private Property<Boolean> clean = Property.ofValue(true);

    @Schema(
        title = "Offset",
        description = "Number of items that should be skipped at the start. The default value is 0."
    )
    @Builder.Default
    private Property<Integer> offset = Property.ofValue(0);

    @Schema(
        title = "Limit",
        description = "Maximum number of items to return. By default Limit value is set to 1000."
    )
    @Builder.Default
    private Property<Integer> limit = Property.ofValue(1000);

    @Schema(
        title = "Fields",
        description = "List of fields which should be picked from the returned items, only these fields will remain in the resulting record objects."
    )
    private Property<List<String>> fields;

    @Schema(
        title = "Omit",
        description = "List of fields which should be omitted from the returned items."
    )
    private Property<List<String>> omit;

    @Schema(
        title = "Unwind",
        description = "A list of fields which should be unwound, in order which they should be " +
            "processed. Each field should be either an array or an object. If the field is an array then every " +
            "element of the array will become a separate record and merged with parent object. If the unwound field " +
            "is an object then it is merged with the parent object. If the unwound field is missing or its value " +
            "is neither an array nor an object and therefore cannot be merged with a parent object then the item gets " +
            "preserved as it is. Note that the unwound items ignore the desc parameter."
    )
    private Property<List<String>> unwind;

    @Schema(
        title = "Flatten",
        description = "List of fields which should transform nested objects into flat structures. For example, with " +
            "flatten=\"foo\" the object {\"foo\":{\"bar\": \"hello\"}} is turned into {\"foo.bar\": \"hello\"}."
    )
    @Builder.Default
    private Property<Boolean> flatten = Property.ofValue(false);

    @Schema(
        title = "sort",
        description = "Sort the runs by startedAt in descending order. Defaults to `ASC`."
    )
    @Builder.Default
    private Property<ApifySortDirection> sort = Property.ofValue(ApifySortDirection.ASC);

    @Schema(
        title = "SkipEmpty",
        description = "If true then empty items are skipped from the output. Default value is true."
    )
    @Builder.Default
    private Property<Boolean> skipEmpty = Property.ofValue(true);

    @Schema(
        title = "SkipFailedPages",
        description = "If true then, the all the items with errorInfo property will be skipped from the output. Default value false."
    )
    @Builder.Default
    private Property<Boolean> skipFailedPages = Property.ofValue(false);

    @Schema(
        title = "View",
        description = "Defines the view configuration for dataset items based on the schema definition. This " +
            "parameter determines how the data will be filtered and presented. For complete specification details, " +
            "see the dataset schema documentation in the Apify documentation."
    )
    private Property<String> view;

    @Schema(
        title = "SkipHidden",
        description = "If true then hidden fields are skipped from the output, i.e. fields starting with the # character."
    )
    @Builder.Default
    private Property<Boolean> skipHidden = Property.ofValue(false);


    @Schema(
        title = "Simplified",
        description = "If true then hidden fields are skipped from the output, i.e. fields starting with the # character."
    )
    @Builder.Default
    private Property<Boolean> simplified = Property.ofValue(false);

    @Builder.Default
    protected Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(300);

    public String buildURL(RunContext runContext) throws IllegalVariableEvaluationException {
        String rDatasetId = runContext.render(this.datasetId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("datasetId is required")
        );

        List<String> rUnwind = runContext.render(this.unwind).asList(String.class);
        List<String> rFields = runContext.render(this.fields).asList(String.class);
        List<String> rOmit = runContext.render(this.omit).asList(String.class);
        Optional<String> rView = runContext.render(this.view).as(String.class);

        final Map<String, Object> queryParamValues = new HashMap<>(Map.of(
            "cleanValue", runContext.render(this.clean).as(Boolean.class).orElseThrow(),
            "offset", runContext.render(this.offset).as(Integer.class).orElseThrow(),
            "sortDirection", runContext.render(this.sort).as(ApifySortDirection.class)
                .orElseThrow() == ApifySortDirection.DESC,
            "flatten", runContext.render(this.flatten).as(Boolean.class).orElseThrow(),
            "skipEmpty", runContext.render(this.skipEmpty).as(Boolean.class).orElseThrow(),
            "limit", runContext.render(this.limit).as(Integer.class).orElseThrow(),
            "simplified", runContext.render(this.simplified).as(Boolean.class).orElseThrow(),
            "skipFailedPages", runContext.render(this.skipFailedPages).as(Boolean.class).orElseThrow(),
            "skipHidden", runContext.render(this.skipHidden).as(Boolean.class).orElseThrow()
        ));

        if (!rFields.isEmpty()) {
            queryParamValues.put("fields", String.join(",", rFields));
        }

        if (!rOmit.isEmpty()) {
            queryParamValues.put("omit", String.join(", ", rOmit));
        }

        if (!rUnwind.isEmpty()) {
            queryParamValues.put("unwind", String.join(", ", rUnwind));
        }

        rView.ifPresent(s -> queryParamValues.put("view", s));

        String basePath = String.format("/datasets/%s/items", rDatasetId);
        return addQueryParams(basePath, queryParamValues);
    }
}
