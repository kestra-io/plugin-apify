package io.kestra.plugin.apify.dataset;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.retrys.Exponential;
import io.kestra.core.runners.RunContext;
import io.kestra.core.utils.RetryUtils;
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
import java.util.function.Predicate;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractGetDataset extends ApifyConnection {
    @Schema(
        title = "Dataset ID",
        description = "Apify dataset identifier to read items from."
    )
    @NotNull
    private Property<String> datasetId;


    @Schema(
        title = "Clean items",
        description = "Skip empty records and fields prefixed with # when true; default true."
    )
    @Builder.Default
    private Property<Boolean> clean = Property.ofValue(true);

    @Schema(
        title = "Offset",
        description = "Number of leading items to skip; default 0."
    )
    @Builder.Default
    private Property<Integer> offset = Property.ofValue(0);

    @Schema(
        title = "Limit",
        description = "Maximum items to return; defaults to 1000."
    )
    @Builder.Default
    private Property<Integer> limit = Property.ofValue(1000);

    @Schema(
        title = "Fields",
        description = "Comma-separated fields to keep in each item; others are dropped."
    )
    private Property<List<String>> fields;

    @Schema(
        title = "Omit",
        description = "Comma-separated fields to remove from each item."
    )
    private Property<List<String>> omit;

    @Schema(
        title = "Unwind fields",
        description = "Fields to unwind in order; array elements become separate records, objects merge into parents. Unwound items ignore the sortDirection flag."
    )
    private Property<List<String>> unwind;

    @Schema(
        title = "Flatten fields",
        description = "Fields to flatten into dotted keys (foo.bar) instead of nested objects."
    )
    @Builder.Default
    private Property<Boolean> flatten = Property.ofValue(false);

    @Schema(
        title = "Sort direction",
        description = "Set to DESC to return newest items first; default ASC."
    )
    @Builder.Default
    private Property<ApifySortDirection> sort = Property.ofValue(ApifySortDirection.ASC);

    @Schema(
        title = "Skip empty items",
        description = "Drop empty records when true; default true."
    )
    @Builder.Default
    private Property<Boolean> skipEmpty = Property.ofValue(true);

    @Schema(
        title = "Skip failed pages",
        description = "Skip items containing errorInfo when true; default false."
    )
    @Builder.Default
    private Property<Boolean> skipFailedPages = Property.ofValue(false);

    @Schema(
        title = "View",
        description = "Dataset view name to filter and project items per the Apify schema."
    )
    private Property<String> view;

    @Schema(
        title = "Skip hidden",
        description = "Skip fields prefixed with # when true; default false."
    )
    @Builder.Default
    private Property<Boolean> skipHidden = Property.ofValue(false);


    @Schema(
        title = "Simplified output",
        description = "Enable Apify simplified output mode; default false."
    )
    @Builder.Default
    private Property<Boolean> simplified = Property.ofValue(false);

    @Builder.Default
    protected Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(300);

    @Builder.Default
    protected Duration DEFAULT_MAX_INTERVAL_DURATION = Duration.ofSeconds(32);

    public String buildURL(RunContext runContext) throws IllegalVariableEvaluationException {
        String rDatasetId = runContext.render(this.datasetId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("datasetId is required")
        );

        List<String> rUnwind = runContext.render(this.unwind).asList(String.class);
        List<String> rFields = runContext.render(this.fields).asList(String.class);
        List<String> rOmit = runContext.render(this.omit).asList(String.class);
        Optional<String> rView = runContext.render(this.view).as(String.class);

        final Map<String, Object> queryParamValues = new HashMap<>(Map.of(
            "cleanValue", runContext.render(this.clean).as(Boolean.class).orElse(true),
            "offset", runContext.render(this.offset).as(Integer.class).orElse(0),
            "sortDirection", runContext.render(this.sort).as(ApifySortDirection.class)
                .orElse(ApifySortDirection.ASC) == ApifySortDirection.DESC,
            "flatten", runContext.render(this.flatten).as(Boolean.class).orElse(false),
            "skipEmpty", runContext.render(this.skipEmpty).as(Boolean.class).orElse(true),
            "limit", runContext.render(this.limit).as(Integer.class).orElse(1000),
            "simplified", runContext.render(this.simplified).as(Boolean.class).orElse(false),
            "skipFailedPages", runContext.render(this.skipFailedPages).as(Boolean.class).orElse(false),
            "skipHidden", runContext.render(this.skipHidden).as(Boolean.class).orElse(false)
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

    protected <T> T withRetry(
        RunContext runContext,
        Predicate<T> retryIfPredicate,
        RetryUtils.CheckedSupplier<T> run
    ) throws Exception {
        Exponential.ExponentialBuilder<?, ?> builder = Exponential.builder()
            .delayFactor(2.0)
            .interval(Duration.ofSeconds(2))
            .maxAttempts(-1)
            .maxInterval(DEFAULT_MAX_INTERVAL_DURATION);

        Duration timeout = runContext.render(this.timeout).as(Duration.class).orElse(null);
        builder.maxDuration(timeout != null ? timeout : DEFAULT_TIMEOUT_DURATION);


        return new RetryUtils().<T, Exception>of(
            builder.build(),
            (RetryUtils.RetryFailed retryFailed) -> {
                throw new IllegalStateException("Timeout reached before dataset was available, please try again " +
                    "later or increase the timeout duration of the task.");
            }
        ).run(retryLoggerWrapper(retryIfPredicate, runContext), run);
    }

    private static <T> Predicate<T> retryLoggerWrapper(Predicate<T> retryIfPredicate, RunContext runContext) {
        return (T value) -> {
            boolean retry = retryIfPredicate.test(value);
            if (retry) {
                runContext.logger().debug("Received empty dataset.");
            }

            return retry;
        };
    }
}
