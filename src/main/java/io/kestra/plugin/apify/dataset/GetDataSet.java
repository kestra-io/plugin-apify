package io.kestra.plugin.apify.dataset;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.ApifyConnection;
import io.kestra.plugin.apify.ApifySortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class GetDataSet extends ApifyConnection {
    @Schema(
        title = "datasetId"
    )
    @NotNull
    private Property<String> datasetId;


    @Schema(
        title = "Clean",
        description = "If true then the task returns only non-empty items and skips hidden fields (i.e. fields starting with the # character). The default value is true."
    )
    private Property<Boolean> clean;

    @Schema(
        title = "Offset",
        description = "Number of items that should be skipped at the start. The default value is 0."
    )
    private Property<Integer> offset;

    @Schema(
        title = "Limit",
        description = "Maximum number of items to return. By default Limit value is set to 1000."
    )
    private Property<Integer> limit;

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
    private Property<Boolean> flatten;

    @Schema(
        title = "sort",
        description = "Sort the runs by startedAt in descending order. Defaults to `ASC`."
    )
    private Property<ApifySortDirection> sort;

    @Schema(
        title = "SkipEmpty",
        description = "If true then empty items are skipped from the output. Default value is true."
    )
    private Property<Boolean> skipEmpty;

    @Schema(
        title = "SkipFailedPages",
        description = "If true then, the all the items with errorInfo property will be skipped from the output. Default value false."
    )
    private Property<Boolean> skipFailedPages;

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
    private Property<Boolean> skipHidden;


    @Schema(
        title = "Simplified",
        description = "If true then hidden fields are skipped from the output, i.e. fields starting with the # character."
    )
    private Property<Boolean> simplified;

    /**
     * Maximum number of calls that will be made if we keep getting empty dataset responses.
     */
    protected static final int MAX_CALL_ATTEMPTS = 3;

    public String buildURL(RunContext runContext) throws IllegalVariableEvaluationException {
        String datasetId = runContext.render(this.datasetId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("datasetId is required")
        );
        boolean cleanValue = runContext.render(this.clean).as(Boolean.class).orElse(true);
        int offset = runContext.render(this.offset).as(Integer.class).orElse(0);
        boolean sortDirection = runContext.render(this.sort).as(ApifySortDirection.class).orElse(ApifySortDirection.ASC) == ApifySortDirection.DESC;
        List<String> unwind = runContext.render(this.unwind).asList(String.class);
        boolean flatten = runContext.render(this.flatten).as(Boolean.class).orElse(false);
        boolean skipEmpty = runContext.render(this.skipEmpty).as(Boolean.class).orElse(true);
        List<String> fields = runContext.render(this.fields).asList(String.class);
        List<String> omit = runContext.render(this.omit).asList(String.class);
        Integer limit = runContext.render(this.limit).as(Integer.class).orElse(1000);
        boolean skipFailedPages = runContext.render(this.skipFailedPages).as(Boolean.class).orElse(false);
        boolean skipHidden = runContext.render(this.skipHidden).as(Boolean.class).orElse(false);
        Optional<String> view = runContext.render(this.view).as(String.class);
        boolean simplified = runContext.render(this.simplified).as(Boolean.class).orElse(false);

        String basePath = String.format("/datasets/%s/items", datasetId);

        final Map<String, Object> queryParamValues = new HashMap<>();

        queryParamValues.put("datasetId", datasetId);
        queryParamValues.put("cleanValue", cleanValue);
        queryParamValues.put("offset", offset);
        queryParamValues.put("sortDirection", sortDirection);
        queryParamValues.put("flatten", flatten);
        queryParamValues.put("skipEmpty", skipEmpty);
        queryParamValues.put("limit", limit);
        queryParamValues.put("simplified", simplified);
        queryParamValues.put("skipFailedPages", skipFailedPages);
        queryParamValues.put("skipHidden", skipHidden);



        if (!fields.isEmpty()) {
            queryParamValues.put("fields", String.join(",", fields));
        }

        if (!omit.isEmpty()) {
            queryParamValues.put("omit", String.join(", ", omit));
        }

        if (!unwind.isEmpty()) {
            queryParamValues.put("unwind", String.join(", ", unwind));
        }

        view.ifPresent(s -> queryParamValues.put("view", s));

        return addQueryParams(basePath, queryParamValues);
    }
}
