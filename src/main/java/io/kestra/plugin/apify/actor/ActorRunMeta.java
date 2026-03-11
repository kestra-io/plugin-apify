package io.kestra.plugin.apify.actor;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class ActorRunMeta {
    private String origin;
    private String clientIp;
    private String userAgent;

    public Optional<String> getClientIp() {
        return Optional.ofNullable(clientIp);
    }
}
