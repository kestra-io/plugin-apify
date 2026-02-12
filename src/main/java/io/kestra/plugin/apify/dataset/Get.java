package io.kestra.plugin.apify.dataset;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Fetch Apify dataset items",
    description = "Polls the Apify dataset items API until data is available or the 300s timeout elapses, using exponential backoff. Empty responses are retried because the actor may still be writing items."
)
@Plugin(
    examples = {
        @Example(
            title = "Get dataset with a given id.",
            full = true,
            code = """
                   id: apify_get_dataset_flow_required_properties
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.dataset.Get
                       apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                       datasetId: mecGriFjtDHRNtYOZ
                   """
        ),
        @Example(
            title = "Get dataset with a given id and specific options.",
            full = true,
            code = """
                   id: apify_get_dataset_flow
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.dataset.Get
                       apiToken: "{{ secret('APIFY_API_TOKEN') }}"
                       datasetId: RNtYOZmecGriFjtDH
                       clean: false
                       offset: 1
                       limit: 10
                       fields: userId, #id, #createdAt, postMeta
                       omit: #id
                       flatten: postMeta
                       sort: ASC
                       skipEmpty: false
                   """
        )
    }
)
public class Get extends AbstractGetDataset implements RunnableTask<Get.Output> {
    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpRequest.HttpRequestBuilder requestBuilder = this.buildGetRequest(
            this.buildURL(runContext)
        );

        List<?> dataset = withRetry(
            runContext,
            Collection::isEmpty,
            () -> this.makeCall(runContext, requestBuilder, List.class)
        );
        return new Output(dataset);
    }

    public record Output(List<?> dataset) implements io.kestra.core.models.tasks.Output {
    }
}
