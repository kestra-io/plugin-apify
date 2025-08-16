package io.kestra.plugin.apify.actor;

import java.util.Optional;

public class ActorRunOptions {
    private Double build;
    private Double timeoutSecs;
    private Double memoryMbytes;
    private Double diskMbytes;
    private Double maxTotalChargeUsd;

    public Optional<Double> getMaxTotalChargeUsd() {
        return Optional.of(maxTotalChargeUsd);
    }
}
