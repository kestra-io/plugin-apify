package io.kestra.plugin.apify.actor.pricinginfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Optional;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "pricingModel", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PricePerEventActorPricingInfo.class, name = "PAY_PER_EVENT"),
    @JsonSubTypes.Type(value = PricePerDatasetItemActorPricingInfo.class, name = "PRICE_PER_DATASET_ITEM"),
    @JsonSubTypes.Type(value = FlatPricePerMonthActorPricingInfo.class, name = "FLAT_PRICE_PER_MONTH"),
    @JsonSubTypes.Type(value = FreeActorPricingInfo.class, name = "FREE")
})
@Data
@JsonIgnoreProperties
public abstract class CommonActorPricingInfo {
    private String pricingModel;
    /** In [0, 1], fraction of pricePerUnitUsd that goes to Apify */
    private Double apifyMarginPercentage;
    /** When this pricing info record has been created */
    private String createdAt;
    /** Since when is this pricing info record effective for a given Actor */
    private String startedAt;
    private String notifiedAboutFutureChangeAt;
    private String notifiedAboutChangeAt;
    private String reasonForChange;

    public Optional<Double> getApifyMarginPercentage() {
        return Optional.ofNullable(apifyMarginPercentage);
    }

    public Optional<String> getNotifiedAboutFutureChangeAt() {
        return Optional.ofNullable(notifiedAboutFutureChangeAt);
    }

    public Optional<String> getNotifiedAboutChangeAt() {
        return Optional.ofNullable(notifiedAboutChangeAt);
    }

    public Optional<String> getReasonForChange() {
        return Optional.ofNullable(reasonForChange);
    }
}
