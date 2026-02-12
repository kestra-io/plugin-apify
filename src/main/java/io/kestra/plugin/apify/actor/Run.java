package io.kestra.plugin.apify.actor;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
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
@Schema(
    title = "Start an Apify actor run",
    description = "Triggers an Apify actor with optional input and run caps, adding query parameters only when set. Returns the run detail from Apify."
)
@Plugin(
    examples = {
        @Example(
            title = "Save dataset with given id as temp file.",
            full = true,
            code = """
                    id: run_actor
                    namespace: company.team

                    tasks:
                      - id: run_actor
                        type: io.kestra.plugin.apify.actor.Run
                        actorId: GdWCkxBtKWOsKjdch
                        maxItems: 1
                        apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                        input:
                          excludePinnedPosts: false
                          hashtags: ["fyp"]
                          resultsPerPage: 2
                   """
        )
    }
)
public class Run extends ApifyConnection implements RunnableTask<ActorRun> {
    @Schema(
        title = "Actor ID",
        description = "Actor ID or owner~actor name to execute."
    )
    @NotNull
    private Property<String> actorId;

    @Schema(
        title = "Input",
        description = "JSON payload passed to the actor; omitted when empty so the actor uses its default input."
    )
    private Property<Map<String, Object>> input;

    @Schema(
        title = "Timeout (seconds)",
        description = "Actor run timeout override in seconds; falls back to the actor's default if unset."
    )
    private Property<Double> requestTimeout;

    @Schema(
        title = "Memory (MB)",
        description = "Memory limit in megabytes (powers of two, minimum 128); uses actor default when omitted."
    )
    private Property<Double> memory;

    @Schema(
        title = "Max items",
        description = "Cap number of items returned to control pay-per-result charges."
    )
    private Property<Integer> maxItems;

    @Schema(
        title = "Max total charge (USD)",
        description = "Maximum allowed cost for the run; stops charges beyond this ceiling."
    )
    private Property<Double> maxTotalChargeUsd;

    @Schema(
        title = "Build",
        description = "Build tag or number to run; defaults to the actor's configured build (typically latest)."
    )
    private Property<String> build;

    @Schema(
        title = "Wait for finish (seconds)",
        description = "Seconds to wait synchronously for completion (0–60); default 0 returns a transitional status if still running."
    )
    private Property<Double> waitForFinish;

    @Schema(
        title = "Webhooks",
        description = "Base64-encoded JSON array describing webhooks for lifecycle events."
    )
    private Property<String> webhooks;

    private static final Logger log = LoggerFactory.getLogger(Run.class);

    @Override
    public ActorRun run(RunContext runContext) throws Exception {
        String rActorId = runContext.render(this.actorId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("actorId is required")
        );

        Map<String, Object> rInput = runContext.render(this.input).asMap(String.class, Object.class);
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
            addQueryParams(String.format("acts/%s/runs", rActorId), filteredQueryParams),
            rInput
        );


        return makeCall(
            runContext, requestBuilder, ActorRunApiResponseWrapper.class
        ).getData();
    }
}
