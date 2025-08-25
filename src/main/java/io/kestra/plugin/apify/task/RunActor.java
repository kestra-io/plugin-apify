package io.kestra.plugin.apify.task;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.ApifyConnection;
import io.kestra.plugin.apify.actor.ActorRun;
import io.kestra.plugin.apify.actor.ActorRunApiResponseWrapper;
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
    title = "Run Apify Actor"
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
                        type: io.kestra.plugin.apify.task.RunActor
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
public class RunActor extends ApifyConnection implements RunnableTask<ActorRun> {
    @Schema(
        title = "Actor ID",
        description = "Actor ID or a tilde-separated owner's username and Actor name."
    )
    @NotNull
    private Property<String> actorId;

    @Schema(
        title = "Input",
        description = "Input for the Actor run. The input is optional and can be used to pass data to the Actor." +
            "If no value is provided then the Actor will run with default run configuration for the Actor. "
    )
    private Property<Map<String, Object>> input;

    @Schema(
        title = "Timeout",
        description = "Optional timeout for the run, in seconds. By default, the run uses a timeout specified in the " +
            "default run configuration for the Actor."
    )
    private Property<Double> requestTimeout;

    @Schema(
        title = "Memory",
        description = "Memory limit for the run, in megabytes. The amount of memory can be set to a power of 2 " +
            "with a minimum of 128. By default, the run uses a memory limit specified in the default run " +
            "configuration for the Actor."
    )
    private Property<Double> memory;

    @Schema(
        title = "Max items",
        description = "The maximum number of items that the Actor run should return. This is useful for " +
            "pay-per-result Actors, as it allows you to limit the number of results that will be charged " +
            "to your subscription. "
    )
    private Property<Integer> maxItems;

    @Schema(
        title = "Max total charge USD",
        description = "Specifies the maximum cost of the Actor run. This parameter is useful for " +
            "pay-per-event Actors, as it allows you to limit the amount charged to your subscription."
    )
    private Property<Double> maxTotalChargeUsd;

    @Schema(
        title = "Build",
        description = "Specifies the Actor build to run. It can be either a build tag or build number. By default, " +
            "the run uses the build specified in the default run configuration for the Actor (typically latest)."
    )
    private Property<String> build;

    @Schema(
        title = "Wait for finish",
        description = "The maximum number of seconds the server waits for the run to finish. By default, it is 0," +
            " the maximum value is 60. If the run finishes in time then the returned run object will have a terminal" +
            " status (e.g. SUCCEEDED), otherwise it will have a transitional status (e.g. RUNNING)."
    )
    private Property<Double> waitForFinish;

    @Schema(
        title = "Webhooks",
        description = "Specifies optional webhooks associated with the Actor run, which can be used to receive a " +
            "notification e.g. when the Actor finished or failed. The value is a Base64-encoded JSON array of " +
            "objects defining the webhooks."
    )
    private Property<String> webhooks;

    private static final Logger log = LoggerFactory.getLogger(RunActor.class);

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
