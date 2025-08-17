package io.kestra.plugin.apify.actor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActorRunApiResponseWrapper {
    private ActorRun data;
}
