package io.kestra.plugin.apify;

import io.kestra.core.models.property.Property;
import io.swagger.v3.oas.annotations.media.Schema;

public interface ApifyConnectionInterface {
    @Schema(
        title = "Apify API token",
        description = "Api Token for Apify. You can find it in your Apify account settings."
    )
    Property<String> getApiToken();
}
