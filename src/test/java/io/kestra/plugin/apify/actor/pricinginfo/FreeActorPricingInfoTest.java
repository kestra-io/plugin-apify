package io.kestra.plugin.apify.actor.pricinginfo;

import org.junit.jupiter.api.Test;

class FreeActorPricingInfoTest extends CommonActorPricingInfoTest {

    @Test
    @Override
    protected void testDeserialization() throws Exception {
        FreeActorPricingInfo freeActorPricingInfo = objectMapper.readValue(
            getJson(),
            FreeActorPricingInfo.class
        );
        super.testPricePerEventActorPricingInfoDeserialization(freeActorPricingInfo);
    }

    @Test
    protected void testSerialization() throws Exception {
        testSerialization(FreeActorPricingInfo.class);
    }

    @Override
    protected String getPricingModel() {
        return "FREE";
    }

    @Override
    protected String getImplementationSpecificProperties() {
        return null;
    }
}