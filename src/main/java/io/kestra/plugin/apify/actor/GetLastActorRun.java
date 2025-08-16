package io.kestra.plugin.apify.actor;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.apify.ApifyConnection;
import io.swagger.v3.oas.annotations.media.Schema;
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
            title = "Get details abut the last actor run for actorId: GdWCkxBtKWOsKjdch.",
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
public class GetLastActorRun extends ApifyConnection implements RunnableTask<ActorRun>  {
    private Property<String> actorId;


    @Override
    public ActorRun run(RunContext runContext) throws Exception {
        String actorId = runContext.render(this.actorId).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("actorId is required")
        );

        HttpRequest.HttpRequestBuilder requestBuilder = buildGetRequest(
            String.format("acts/%s/runs/last", actorId)
        );
        return makeCall(runContext, requestBuilder, ActorRunApiResponseWrapper.class).data;
    }
}
