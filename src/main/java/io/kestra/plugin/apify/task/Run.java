package io.kestra.plugin.apify.task;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.ApifyConnection;
import io.kestra.plugin.apify.actor.ActorRun;
import io.kestra.plugin.apify.actor.ActorRunApiResponseWrapper;
import io.kestra.plugin.apify.actor.MemoryMbytes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Start an Apify Task run",
    description = """
        Triggers an Apify Task (an actor with predefined configuration) with optional input overrides and run caps,
        adding query parameters only when set. Returns the run detail from Apify.
        """
)
@Plugin(
    examples = {
        @Example(
            title = "Run an Apify Task by its ID.",
            full = true,
            code = """
                id: run_apify_task
                namespace: company.team

                tasks:
                  - id: run_task
                    type: io.kestra.plugin.apify.task.Run
                    taskId: my_username~my-task
                    apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                """
        ),
        @Example(
            title = "Run an Apify Task with input overrides and a synchronous wait.",
            full = true,
            code = """
                id: run_apify_task_with_input
                namespace: company.team

                tasks:
                  - id: run_task
                    type: io.kestra.plugin.apify.task.Run
                    taskId: my_username~my-task
                    apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                    maxItems: 100
                    waitForFinish: 60
                    input:
                      startUrls:
                        - url: "https://example.com"
                """
        )
    }
)
public class Run extends ApifyConnection implements RunnableTask<ActorRun> {
    @Schema(
        title = "Task ID",
        description = "Apify Task ID or owner~task-name to execute."
    )
    @NotNull
    private Property<String> taskId;

    @Schema(
        title = "Input",
        description = "JSON payload to override the task's predefined input; omitted when empty so the task uses its saved input."
    )
    private Property<Map<String, Object>> input;

    @Schema(
        title = "Timeout (seconds)",
        description = "Actor run timeout override in seconds; falls back to the task's default if unset."
    )
    private Property<Double> requestTimeout;

    @Schema(
        title = "Memory (MB)",
        description = "Memory allocation for the run; must be a power of two between 128 MB and 32768 MB."
    )
    private Property<MemoryMbytes> memory;

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
        description = "Seconds to wait synchronously for the run to complete (0–60); 0 returns immediately with a transitional status if still running. Maximum is 60 seconds."
    )
    private Property<Integer> waitForFinish;

    @Schema(
        title = "Webhooks",
        description = "Base64-encoded JSON array describing webhooks for lifecycle events."
    )
    private Property<String> webhooks;

    @Override
    public ActorRun run(RunContext runContext) throws Exception {
        var rTaskId = runContext.render(this.taskId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("taskId is required")
        );

        var rInput = runContext.render(this.input).asMap(String.class, Object.class);
        Map<String, Optional<?>> queryParams = Map.of(
            "timeout", runContext.render(this.requestTimeout).as(Double.class),
            "memory", runContext.render(this.memory).as(MemoryMbytes.class),
            "maxItems", runContext.render(this.maxItems).as(Integer.class),
            "maxTotalChargeUsd", runContext.render(this.maxTotalChargeUsd).as(Double.class),
            "build", runContext.render(this.build).as(String.class),
            "waitForFinish", runContext.render(this.waitForFinish).as(Integer.class),
            "webhooks", runContext.render(this.webhooks).as(String.class)
        );

        var filteredQueryParams = queryParams.entrySet().stream().filter(
            entry -> entry.getValue().isPresent()
        ).collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()
            )
        );

        HttpRequest.HttpRequestBuilder requestBuilder = buildPostRequest(
            addQueryParams(String.format("actor-tasks/%s/runs", rTaskId), filteredQueryParams),
            rInput
        );

        return makeCall(
            runContext, requestBuilder, ActorRunApiResponseWrapper.class
        ).getData();
    }
}
