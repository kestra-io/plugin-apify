package io.kestra.plugin.apify.actor.pricinginfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Optional;

@Data
@JsonIgnoreProperties
public class PricePerDatasetItemActorPricingInfo extends CommonActorPricingInfo {
    private String unitName;
    private Double pricePerUnitUsd;

    public Optional<String> getUnitName() {
        return Optional.ofNullable(unitName);
    }
}
