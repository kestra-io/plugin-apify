package io.kestra.plugin.apify.actor.pricinginfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.serializers.JacksonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class CommonActorPricingInfoTest {
    protected static final ObjectMapper objectMapper = JacksonMapper.ofJson(false);
    private static final String BASE_JSON_TEMPLATE = """
            {
                "pricingModel": "[PRICING_MODEL]",
                "apifyMarginPercentage": 0.1,
                "createdAt": "2023-10-01T00:00:00Z",
                "startedAt": "2023-10-01T00:00:00Z",
                "notifiedAboutFutureChangeAt": "2025-07-05T12:05:00.000Z",
                "notifiedAboutChangeAt": "2025-06-05T12:05:00.000Z",
                "reasonForChange": "reasonForChange", [IMPLEMENTATION_SPECIFIC_PROPERTIES]
            }
            """;

    void testPricePerEventActorPricingInfoDeserialization(CommonActorPricingInfo commonActorPricingInfo) throws Exception {
        assertEquals(getPricingModel(), commonActorPricingInfo.getPricingModel());

        assertTrue(commonActorPricingInfo.getApifyMarginPercentage().isPresent());
        assertEquals(0.1, commonActorPricingInfo.getApifyMarginPercentage().get());

        assertEquals("2023-10-01T00:00:00Z", commonActorPricingInfo.getCreatedAt());
        assertEquals("2023-10-01T00:00:00Z", commonActorPricingInfo.getStartedAt());

        assertTrue(commonActorPricingInfo.getNotifiedAboutFutureChangeAt().isPresent());
        assertEquals("2025-07-05T12:05:00.000Z", commonActorPricingInfo.getNotifiedAboutFutureChangeAt().get());

        assertTrue(commonActorPricingInfo.getNotifiedAboutChangeAt().isPresent());
        assertEquals("2025-06-05T12:05:00.000Z", commonActorPricingInfo.getNotifiedAboutChangeAt().get());

        assertTrue(commonActorPricingInfo.getReasonForChange().isPresent());
        assertEquals("reasonForChange", commonActorPricingInfo.getReasonForChange().get());
    }

    protected abstract void testDeserialization() throws Exception;


    /**
     * Tests the serialization and deserialization of the CommonActorPricingInfo object.
     * @param clazz The class of the CommonActorPricingInfo object to test.
     */
    protected <T extends CommonActorPricingInfo> void testSerialization(Class<T> clazz) throws Exception {
        T pricingInfo = objectMapper.readValue(this.getJson(), clazz);
        assertInstanceOf(clazz, pricingInfo);

        String serialisedJson = objectMapper.writeValueAsString(pricingInfo);
        assertEquals(getJson().replaceAll("[\\n ]", ""), serialisedJson);
    };

    /**
     * @return Returns the pricing model used as the key for Polymorphic serialization of CommonActorPricingInfo objects
     */
    abstract protected String getPricingModel();

    /**
     * @return Returns implementation specific properties as a JSON string without brackets.
     * If the implementation does not have any implementation specific properties, return null.
     */
    abstract protected String getImplementationSpecificProperties();

    protected String getJson() {
        String json =  BASE_JSON_TEMPLATE
            .replace("[PRICING_MODEL]", getPricingModel());

        if (getImplementationSpecificProperties() == null) {
            return json.replace(", [IMPLEMENTATION_SPECIFIC_PROPERTIES]", "");
        }
        return json.replace("[IMPLEMENTATION_SPECIFIC_PROPERTIES]", getImplementationSpecificProperties());
    }
}