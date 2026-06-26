package io.kestra.plugin.apify.actor;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.kestra.core.models.tasks.Output;
import io.kestra.plugin.apify.actor.pricinginfo.CommonActorPricingInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class ActorRun extends ActorRunListItem implements Output {
    @Schema(title = "ID of the user that started the run")
    String userId;

    @Schema(title = "Human-readable status message for the run")
    String statusMessage;

    @Schema(title = "Run statistics")
    ActorRunStats stats;

    @Schema(title = "Options the Actor was started with")
    ActorRunOptions options;

    @Schema(title = "Process exit code of the run")
    Double exitCode;

    @Schema(title = "URL of the running Actor container")
    String containerUrl;

    @Schema(title = "Whether the container HTTP server is ready")
    Boolean isContainerServerReady;

    @Schema(title = "Git branch the Actor build was made from")
    String gitBranchName;

    @Schema(title = "Resource usage of the run")
    ActorRunUsage usage;

    @Schema(title = "Resource usage of the run, in USD")
    ActorRunUsage usageUsd;

    @Schema(title = "Pricing model applied to the run")
    CommonActorPricingInfo pricingInfo;

    @Schema(title = "Counts of charged events by event name")
    Map<String, Double> chargedEventCounts;

    @Schema(title = "General access setting for the run")
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
