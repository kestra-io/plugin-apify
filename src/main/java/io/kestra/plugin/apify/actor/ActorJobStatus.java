package io.kestra.plugin.apify.actor;

public enum ActorJobStatus {
    READY,
    RUNNING,
    SUCCEEDED,
    FAILED,
    TIMING_OUT,
    TIMED_OUT,
    ABORTING,
    ABORTED
}
