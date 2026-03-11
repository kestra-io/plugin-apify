package io.kestra.plugin.apify.actor;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class ActorRunOptions {
    private Double build;
    private Double timeoutSecs;
    private Double memoryMbytes;
    private Double diskMbytes;
    private Double maxTotalChargeUsd;

    public Optional<Double> getMaxTotalChargeUsd() {
        return Optional.ofNullable(maxTotalChargeUsd);
    }
}
