package io.kestra.plugin.apify.actor.pricinginfo;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties
public class ActorChargeEvent {
    Double eventPriceUsd;
    String eventTitle;
    String eventDescription;

    public Optional<Double> getEventPriceUsd() {
        return Optional.ofNullable(eventPriceUsd);
    }

    public Optional<String> getEventDescription() {
        return Optional.ofNullable(eventDescription);
    }
}
