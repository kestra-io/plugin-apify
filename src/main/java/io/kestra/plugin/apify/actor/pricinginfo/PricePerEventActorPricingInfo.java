package io.kestra.plugin.apify.actor.pricinginfo;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class PricePerEventActorPricingInfo extends CommonActorPricingInfo {
    private PricingPerEvent pricingPerEvent;
    private Double minimalMaxTotalChargeUsd;

    public Optional<Double> getMinimalMaxTotalChargeUsd() {
        return Optional.ofNullable(minimalMaxTotalChargeUsd);
    }
}
