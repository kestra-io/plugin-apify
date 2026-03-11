package io.kestra.plugin.apify.actor.pricinginfo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class PricingPerEvent {
    private Map<String, ActorChargeEvent> actorChargeEvents;
}
