package io.kestra.plugin.apify.actor.pricinginfo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricePerDatasetItemActorPricingInfoTest extends CommonActorPricingInfoTest {

    @Override
    protected String getPricingModel() {
        return "PRICE_PER_DATASET_ITEM";
    }

    @Override
    protected String getImplementationSpecificProperties() {
        return """
            "unitName": "unitName",
            "pricePerUnitUsd": 0.9
            """;
    }

    @Test
    @Override
    protected void testDeserialization() throws Exception {
        PricePerDatasetItemActorPricingInfo pricePerDatasetItemActorPricingInfo = objectMapper.readValue(
            getJson(),
            PricePerDatasetItemActorPricingInfo.class
        );
        testPricePerEventActorPricingInfoDeserialization(pricePerDatasetItemActorPricingInfo);

        assertTrue(pricePerDatasetItemActorPricingInfo.getUnitName().isPresent());
        assertEquals("unitName", pricePerDatasetItemActorPricingInfo.getUnitName().get());
        assertEquals(0.9, pricePerDatasetItemActorPricingInfo.getPricePerUnitUsd());
    }

    @Test
    protected void testSerialization() throws Exception {
        testSerialization(PricePerDatasetItemActorPricingInfo.class);
    }
}