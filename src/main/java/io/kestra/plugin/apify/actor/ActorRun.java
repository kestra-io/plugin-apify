package io.kestra.plugin.apify.actor;

import io.kestra.core.models.tasks.Output;
import io.kestra.plugin.apify.actor.pricinginfo.CommonActorPricingInfo;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
public class ActorRun extends ActorRunListItem implements Output {
    String userId;
    String statusMessage;
    ActorRunStats stats;
    ActorRunOptions options;
    Double exitCode;
    String containerUrl;
    Boolean isContainerServerReady;
    String gitBranchName;
    ActorRunUsage usage;
    ActorRunUsage usageUsd;
    CommonActorPricingInfo pricingInfo;
    Map<String, Double> chargedEventCounts;
    RunGenralAccess generalAccess;

    public Optional<String> getStatusMessage() {
        return Optional.ofNullable(statusMessage);
    }

    public Optional<String> getGitBranchName() {
        return Optional.ofNullable(gitBranchName);
    }

    public Optional<ActorRunUsage> getUsage() {
        return Optional.ofNullable(usage);
    }

    public Optional<ActorRunUsage> getUsageUsd() {
        return Optional.ofNullable(usageUsd);
    }

    public Optional<CommonActorPricingInfo> getPricingInfo() {
        return Optional.ofNullable(pricingInfo);
    }

    public Optional<Map<String, Double>> getChargedEventCounts() {
        return Optional.ofNullable(chargedEventCounts);
    }

    public Optional<RunGenralAccess> getGeneralAccess() {
        return Optional.ofNullable(generalAccess);
    }
}
