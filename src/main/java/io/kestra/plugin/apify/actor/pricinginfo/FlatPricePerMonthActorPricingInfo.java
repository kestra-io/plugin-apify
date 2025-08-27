package io.kestra.plugin.apify.actor.pricinginfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Optional;

@Data
@JsonIgnoreProperties
public class FlatPricePerMonthActorPricingInfo extends CommonActorPricingInfo{
    Double trialMinutes;
    /** Monthly flat price in USD */
    Double pricePerUnitUsd;

    public Optional<Double> getTrialMinutes() {
        return Optional.ofNullable(trialMinutes);
    }
}
