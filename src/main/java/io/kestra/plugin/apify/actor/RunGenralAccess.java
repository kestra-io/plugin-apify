package io.kestra.plugin.apify.actor;

public enum RunGenralAccess {
    // Respect the user setting of the run owner (default behavior)
    FOLLOW_USER_SETTING,
    /** Only signed-in users with explicit access can read this run. */
    RESTRICTED,
    /** Anyone with a link, or the unique run ID, can read the run. */
    ANYONE_WITH_ID_CAN_READ
}
