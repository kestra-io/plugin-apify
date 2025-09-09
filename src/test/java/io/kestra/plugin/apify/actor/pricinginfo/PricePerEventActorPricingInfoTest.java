package io.kestra.plugin.apify.actor.pricinginfo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricePerEventActorPricingInfoTest extends CommonActorPricingInfoTest {
    @Override
    protected String getPricingModel() {
        return "PAY_PER_EVENT";
    }

    @Override
    protected String getImplementationSpecificProperties() {
        return """
                "pricingPerEvent": {
                    "actorChargeEvents": {
                        "keyOne": {
                            "eventPriceUsd": 0.5,
                            "eventTitle": "keyOneEventTitle",
                            "eventDescription": "keyOneEventDescription"
                        },
                        "keyTwo": {
                            "eventPriceUsd": 0.5,
                            "eventTitle": "keyTwoEventTitle",
                            "eventDescription": "keyTwoEventDescription"
                        }
                    }
                },
                "minimalMaxTotalChargeUsd": 50.0
            """;
    }

    @Test
    @Override
    protected void testDeserialization() throws Exception {
        PricePerEventActorPricingInfo pricingInfo = objectMapper.readValue(this.getJson(), PricePerEventActorPricingInfo.class);
        assertInstanceOf(PricePerEventActorPricingInfo.class, pricingInfo);

        testPricePerEventActorPricingInfoDeserialization(pricingInfo);

        assertTrue(pricingInfo.getMinimalMaxTotalChargeUsd().isPresent());
        assertEquals(50.0, pricingInfo.getMinimalMaxTotalChargeUsd().get());

        assertTrue(pricingInfo.getPricingPerEvent().getActorChargeEvents().containsKey("keyOne"));
        assertEquals(new ActorChargeEvent(0.5, "keyOneEventTitle", "keyOneEventDescription"),
            pricingInfo.getPricingPerEvent().getActorChargeEvents().get("keyOne"));

        assertTrue(pricingInfo.getPricingPerEvent().getActorChargeEvents().containsKey("keyTwo"));
        assertEquals(new ActorChargeEvent(0.5, "keyTwoEventTitle", "keyTwoEventDescription"),
            pricingInfo.getPricingPerEvent().getActorChargeEvents().get("keyTwo"));
    }

    @Test
    protected void testSerialization() throws Exception {
        testSerialization(PricePerEventActorPricingInfo.class);
    }
}