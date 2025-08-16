package io.kestra.plugin.apify.actor.pricinginfo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlatPricePerMonthActorPricingInfoTest extends CommonActorPricingInfoTest {

    @Test
    @Override
    protected void testDeserialization() throws Exception {
        FlatPricePerMonthActorPricingInfo pricingInfo = objectMapper.readValue(
            getJson(),
            FlatPricePerMonthActorPricingInfo.class
        );

        testPricePerEventActorPricingInfoDeserialization(pricingInfo);

        assertTrue(pricingInfo.getTrialMinutes().isPresent());
        assertEquals(1000.0, pricingInfo.getTrialMinutes().get());
        assertEquals(10.0, pricingInfo.getPricePerUnitUsd());
    }

    @Test
    protected void testSerialization() throws Exception {
        testSerialization(FlatPricePerMonthActorPricingInfo.class);
    }

    @Override
    protected String getPricingModel() {
        return "FLAT_PRICE_PER_MONTH";
    }

    @Override
    protected String getImplementationSpecificProperties() {
        return """
            "trialMinutes": 1000.0,
            "pricePerUnitUsd": 10.0
            """;
    }
}