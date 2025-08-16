package io.kestra.plugin.apify.actor;

import lombok.Data;

import java.util.Optional;

@Data
public class ActorRunMeta {
    private String origin;
    private String clientIp;
    private String userAgent;

    public Optional<String> getClientIp() {
        return Optional.ofNullable(clientIp);
    }
}
