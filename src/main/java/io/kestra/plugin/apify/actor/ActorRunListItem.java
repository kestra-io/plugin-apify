package io.kestra.plugin.apify.actor;

import lombok.Data;

import java.util.Optional;

@Data
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
        return Optional.of(actorTaskId);
    }

    Optional<String> getBuildId() {
        return Optional.of(buildId);
    }
}
