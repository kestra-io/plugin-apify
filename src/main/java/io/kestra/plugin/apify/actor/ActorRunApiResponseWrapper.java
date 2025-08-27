package io.kestra.plugin.apify.actor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorRunApiResponseWrapper {
    private ActorRun data;
}
