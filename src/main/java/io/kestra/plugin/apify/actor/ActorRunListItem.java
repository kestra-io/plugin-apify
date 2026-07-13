package io.kestra.plugin.apify.actor;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties
public abstract class ActorRunListItem {
    @Schema(title = "Run ID")
    private String id;

    @Schema(title = "ID of the Actor that was run")
    private String actId;

    @Schema(title = "ID of the Actor task, if the run was started from a task")
    private String actorTaskId;

    @Schema(title = "Time the run started")
    private String startedAt;

    @Schema(title = "Time the run finished")
    private String finishedAt;

    @Schema(title = "Run status")
    private ActorJobStatus status;

    @Schema(title = "Run metadata such as origin and client info")
    private ActorRunMeta meta;

    @Schema(title = "ID of the Actor build used for the run")
    private String buildId;

    @Schema(title = "Build number of the Actor build used for the run")
    private String buildNumber;

    @Schema(title = "ID of the run's default key-value store")
    private String defaultKeyValueStoreId;

    @Schema(title = "ID of the run's default dataset")
    private String defaultDatasetId;

    @Schema(title = "ID of the run's default request queue")
    private String defaultRequestQueueId;

    @Schema(title = "Total usage cost of the run, in USD")
    private String usageTotalUsd;

    Optional<String> getActorTaskId() {
        return Optional.ofNullable(actorTaskId);
    }

    Optional<String> getBuildId() {
        return Optional.ofNullable(buildId);
    }
}
