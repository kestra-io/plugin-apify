package io.kestra.plugin.apify.actor.pricinginfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties
public class PricingPerEvent {
    private Map<String, ActorChargeEvent> actorChargeEvents;
}
