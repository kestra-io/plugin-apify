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
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Unstructured Dataset",
    description = "Get Unstructured Dataset by ID"
)
@Plugin(
    examples = {
        @Example(
            title = "Get details about the last actor run of given actor id.",
            full = true,
            code = """
                   id: get_last_run
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.actor.GetLastActorRun
                       apiToken: your_apify_token
                       actorId: GdWCkxBtKWOsKjdch
                   """
        )
    }
)
public class GetLastActorRun extends ApifyConnection implements RunnableTask<ActorRun> {
    @Schema(
        title = "Actor ID"
    )
    @NotNull
    private Property<String> actorId;


    @Override
    public ActorRun run(RunContext runContext) throws Exception {
        String rActorId = runContext.render(this.actorId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("actorId is required")
        );

        HttpRequest.HttpRequestBuilder requestBuilder = buildGetRequest(
            String.format("acts/%s/runs/last", rActorId)
        );
        return makeCall(runContext, requestBuilder, ActorRunApiResponseWrapper.class).getData();
    }
}
