package io.kestra.plugin.apify.actor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class ActorRunUsage {
    // All Optional and number
    @JsonProperty("ACTOR_COMPUTE_UNITS")
    private Double actorComputeUnits;

    @JsonProperty("DATASET_READS")
    private Double datasetReads;

    @JsonProperty("DATASET_WRITES")
    private Double datasetWrites;

    @JsonProperty("KEY_VALUE_STORE_READS")
    private Double keyValueStoreReads;

    @JsonProperty("KEY_VALUE_STORE_WRITES")
    private Double keyValueStoreWrites;

    @JsonProperty("KEY_VALUE_STORE_LISTS")
    private Double keyValueStoreLists;

    @JsonProperty("REQUEST_QUEUE_READS")
    private Double requestQueueReads;

    @JsonProperty("REQUEST_QUEUE_WRITES")
    private Double requestQueueWrites;

    @JsonProperty("DATA_TRANSFER_INTERNAL_GBYTES")
    private Double dataTransferInternalGbytes;

    @JsonProperty("DATA_TRANSFER_EXTERNAL_GBYTES")
    private Double dataTransferExternalGbytes;

    @JsonProperty("PROXY_RESIDENTIAL_TRANSFER_GBYTES")
    private Double proxyResidentialTransferGbytes;

    @JsonProperty("PROXY_SERPS")
    private Double proxySerps;

    public Optional<Double> getActorComputeUnits() {
        return Optional.ofNullable(actorComputeUnits);
    }

    public Optional<Double> getDatasetReads() {
        return Optional.ofNullable(datasetReads);
    }

    public Optional<Double> getDatasetWrites() {
        return Optional.ofNullable(datasetWrites);
    }

    public Optional<Double> getKeyValueStoreReads() {
        return Optional.ofNullable(keyValueStoreReads);
    }

    public Optional<Double> getKeyValueStoreWrites() {
        return Optional.ofNullable(keyValueStoreWrites);
    }

    public Optional<Double> getKeyValueStoreLists() {
        return Optional.ofNullable(keyValueStoreLists);
    }

    public Optional<Double> getRequestQueueReads() {
        return Optional.ofNullable(requestQueueReads);
    }

    public Optional<Double> getRequestQueueWrites() {
        return Optional.ofNullable(requestQueueWrites);
    }

    public Optional<Double> getDataTransferInternalGbytes() {
        return Optional.ofNullable(dataTransferInternalGbytes);
    }

    public Optional<Double> getDataTransferExternalGbytes() {
        return Optional.ofNullable(dataTransferExternalGbytes);
    }

    public Optional<Double> getProxyResidentialTransferGbytes() {
        return Optional.ofNullable(proxyResidentialTransferGbytes);
    }

    public Optional<Double> getProxySerps() {
        return Optional.ofNullable(proxySerps);
    }
}
