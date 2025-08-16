package io.kestra.plugin.apify.actor;

import lombok.Data;

@Data
public class ActorRunStats {
    private Double inputBodyLen;
    private Double restartCount;
    private Double resurrectCount;
    private Double memAvgBytes;
    private Double memMaxBytes;
    private Double memCurrentBytes;
    private Double cpuAvgUsage;
    private Double cpuMaxUsage;
    private Double cpuCurrentUsage;
    private Double netRxBytes;
    private Double netTxBytes;
    private Double durationMillis;
    private Double runTimeSecs;
    private Double metamorph;
    private Double computeUnits;
}
