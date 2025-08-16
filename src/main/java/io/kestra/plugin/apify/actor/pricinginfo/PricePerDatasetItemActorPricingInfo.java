package io.kestra.plugin.apify.actor.pricinginfo;

import lombok.Data;

import java.util.Optional;

@Data
public class PricePerDatasetItemActorPricingInfo extends CommonActorPricingInfo {
    private String unitName;
    private Double pricePerUnitUsd;

    public Optional<String> getUnitName() {
        return Optional.of(unitName);
    }
}
