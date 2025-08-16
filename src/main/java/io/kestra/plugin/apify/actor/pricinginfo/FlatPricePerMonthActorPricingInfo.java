package io.kestra.plugin.apify.actor.pricinginfo;

import lombok.Data;

import java.util.Optional;

@Data
public class FlatPricePerMonthActorPricingInfo extends CommonActorPricingInfo{
    Double trialMinutes;
    /** Monthly flat price in USD */
    Double pricePerUnitUsd;

    public Optional<Double> getTrialMinutes() {
        return Optional.ofNullable(trialMinutes);
    }
}
