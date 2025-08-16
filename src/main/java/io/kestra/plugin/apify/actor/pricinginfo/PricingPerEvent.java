package io.kestra.plugin.apify.actor.pricinginfo;

import lombok.Data;

import java.util.Map;

@Data
public class PricingPerEvent {
    private Map<String, ActorChargeEvent> actorChargeEvents;
}
