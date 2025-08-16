package io.kestra.plugin.apify.actor.pricinginfo;

import lombok.Data;

import java.util.Optional;

@Data
public class PricePerEventActorPricingInfo extends CommonActorPricingInfo {
    private PricingPerEvent pricingPerEvent;
    private Double minimalMaxTotalChargeUsd;

    public Optional<Double> getMinimalMaxTotalChargeUsd() {
        return Optional.ofNullable(minimalMaxTotalChargeUsd);
    }
}
