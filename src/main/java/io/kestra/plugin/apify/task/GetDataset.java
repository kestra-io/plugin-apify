package io.kestra.plugin.apify.task;

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
import org.slf4j.Logger;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Dataset"
)
@Plugin(
    examples = {
        @Example(
            title = "Get dataset with id of mecGriFjtDHRNtYOZ.",
            full = true,
            code = """
                   id: apify_get_dataset_flow_required_properties
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.task.GetDataset
                       apiToken: your_apify_token
                       datasetId: mecGriFjtDHRNtYOZ
                   """
        ),
        @Example(
            title = "Get dataset with id of RNtYOZmecGriFjtDH.",
            full = true,
            code = """
                   id: apify_get_dataset_flow
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.task.GetDataset
                       apiToken: your_apify_token
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
public class GetDataset extends AbstractGetDataset implements RunnableTask<GetDataset.Output> {
    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger log = runContext.logger();
        HttpRequest.HttpRequestBuilder requestBuilder = this.buildGetRequest(
            this.buildURL(runContext)
        );

        /*
         * It can take several seconds between an actor run finishing and a dataset being fully uploaded.
         * If the user uses both the ActorRun and GetDataset task,
         * we need to retry the request if we get an empty response.
         */
        int attempts = 1;
        while (attempts < MAX_CALL_ATTEMPTS) {
            List<?> dataset = this.makeCall(runContext, requestBuilder, List.class);

            if (!dataset.isEmpty()) {
                return new Output(dataset);
            }

            log.debug("Received empty dataset, will retry again in 5000ms");
            Thread.sleep(5000);
            attempts++;
        }
        throw new IllegalStateException("Failed to get dataset after " + MAX_CALL_ATTEMPTS + " attempts");
    }

    public record Output(List<?> dataset) implements io.kestra.core.models.tasks.Output {
    }
}
