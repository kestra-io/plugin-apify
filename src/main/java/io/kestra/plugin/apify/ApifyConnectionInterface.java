package io.kestra.plugin.apify;

import io.kestra.core.models.property.Property;
import io.swagger.v3.oas.annotations.media.Schema;

public interface ApifyConnectionInterface {
    @Schema(
        title = "Apify API token",
        description = "Personal Apify API token used for all requests; required."
    )
    Property<String> getApiToken();
}
