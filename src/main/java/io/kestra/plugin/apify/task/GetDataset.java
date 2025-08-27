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

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Dataset",
    description = "This task uses short polling to get the dataset from Apify. " +
        "If this tasks received an empty dataset, it will retry the request every 5 seconds until the dataset is " +
        "available or the tasks timeout limit is reached. When this task receives a empty dataset it is typically " +
        "because the actor run has not finished uploading the Dataset."
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
                       type: io.kestra.plugin.apify.task.GetDataset
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
                       type: io.kestra.plugin.apify.task.GetDataset
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
public class GetDataset extends AbstractGetDataset implements RunnableTask<GetDataset.Output> {
    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        HttpRequest.HttpRequestBuilder requestBuilder = this.buildGetRequest(
            this.buildURL(runContext)
        );

        /*
         * It can take several seconds between an actor run finishing and a dataset being fully uploaded.
         * If the user uses both the ActorRun and GetDataset task,
         * we need to retry the request if we get an empty response.
         */

        Instant end = Instant.now().plus(DEFAULT_TIMEOUT_DURATION);
        boolean isTaskTimeoutSet = runContext.render(this.timeout).as(Duration.class).isPresent();
        while (isTaskTimeoutSet || end.isBefore(Instant.now())) {
            List<?> dataset = this.makeCall(runContext, requestBuilder, List.class);

            if (!dataset.isEmpty()) {
                return new Output(dataset);
            }

            log.debug("Received empty dataset, will retry again in 5000ms");
            Thread.sleep(5000);
        }

        throw new IllegalStateException("Timeout reached before dataset was available, please try again later or increase the timeout duration of the task.");
    }

    public record Output(List<?> dataset) implements io.kestra.core.models.tasks.Output {
    }
}
