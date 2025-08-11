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

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Dataset",
    description = "Get Dataset by ID"
)
@Plugin(
    examples = {
        @Example(
            title = "Get dataset with id of mecGriFjtDHRNtYOZ.",
            full = true,
            code = """
                   id: apify_list_runs_flow_required_properties
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.runs.ListRuns
                       apiToken: your_apify_token
                       datasetId: mecGriFjtDHRNtYOZ
                   """
        ),
        @Example(
            title = "Get dataset with id of RNtYOZmecGriFjtDH.",
            full = true,
            code = """
                   id: apify_list_runs_flow_all_properties
                   namespace: company.team

                   tasks:
                     - id: list_runs
                       type: io.kestra.plugin.apify.runs.ListRuns
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
public class GetStructuredDataset extends GetDataSet implements RunnableTask<GetStructuredDataset.Output> {
    @Override
    public Output run(RunContext runContext) throws Exception {
        HttpRequest.HttpRequestBuilder requestBuilder = this.buildGetRequest(
            this.buildURL(runContext)
        );
        List<?> dataset = this.makeCallAndWriteToFile(runContext, requestBuilder, List.class);
        return new Output(dataset);
    }

    public record Output(List<?> dataset) implements io.kestra.core.models.tasks.Output {
    }
}
