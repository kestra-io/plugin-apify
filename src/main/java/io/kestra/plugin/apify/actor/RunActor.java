package io.kestra.plugin.apify.actor;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.ApifyConnection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public class RunActor extends ApifyConnection implements RunnableTask<ActorRun> {
    @Schema(
        title = "datasetId"
    )
    @NotNull
    private Property<String> actorId;

    @Schema(
        title = "Input"
    )
    private Property<Map<String, Object>> input;

    @Schema(
        title = "Timeout"
    )
    private Property<Double> requestTimeout;

    @Schema(
        title = "Memory"
    )
    private Property<Double> memory;

    @Schema(
        title = "Max items"
    )
    private Property<Integer> maxItems;

    @Schema(
        title = "Max total charge USD"
    )
    private Property<Double> maxTotalChargeUsd;

    @Schema(
        title = "Build"
    )
    private Property<String> build;

    @Schema(
        title = "Wait for finish"
    )
    private Property<Double> waitForFinish;

    @Schema(
        title = "Webhooks"
    )
    private Property<String> webhooks;

    private static final Logger log = LoggerFactory.getLogger(RunActor.class);

    @Override
    public ActorRun run(RunContext runContext) throws Exception {
        String actorId = runContext.render(this.actorId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("actorId is required")
        );

        Map<String, Object> input = runContext.render(this.input).asMap(String.class, Object.class);

        log.info("Input: {}", input);

        Map<String, Optional<?>> queryParams = Map.of(
            "timeout", runContext.render(this.requestTimeout).as(Double.class),
            "memory", runContext.render(this.memory).as(Double.class),
            "maxItems", runContext.render(this.maxItems).as(Integer.class),
            "maxTotalChargeUsd", runContext.render(this.maxTotalChargeUsd).as(Double.class),
            "build", runContext.render(this.build).as(String.class),
            "waitForFinish", runContext.render(this.waitForFinish).as(Double.class),
            "webhooks", runContext.render(this.webhooks).as(String.class)
        );

        Map<String, ?> filteredQueryParams = queryParams.entrySet().stream().filter(
            queryParamsEntry -> queryParamsEntry.getValue().isPresent()
        ).collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().get()
        ));

        HttpRequest.HttpRequestBuilder requestBuilder = buildPostRequest(
            addQueryParams(String.format("acts/%s/runs", actorId), filteredQueryParams),
            input
        );


        ActorRun actorRun = makeCall(
            runContext, requestBuilder, ActorRunApiResponseWrapper.class
        ).getData();

        return actorRun;
    }
}
