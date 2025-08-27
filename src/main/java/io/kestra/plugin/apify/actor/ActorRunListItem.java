package io.kestra.plugin.apify.actor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Optional;

@Data
@JsonIgnoreProperties
public abstract class ActorRunListItem {
    private String id;
    private String actId;
    private String actorTaskId;
    private String startedAt;
    private String finishedAt;
    private ActorJobStatus status;
    private ActorRunMeta meta;
    private String buildId;
    private String buildNumber;
    private String defaultKeyValueStoreId;
    private String defaultDatasetId;
    private String defaultRequestQueueId;
    private String usageTotalUsd;

    Optional<String> getActorTaskId() {
        return Optional.ofNullable(actorTaskId);
    }

    Optional<String> getBuildId() {
        return Optional.ofNullable(buildId);
    }
}
