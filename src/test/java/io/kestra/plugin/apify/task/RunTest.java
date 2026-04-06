package io.kestra.plugin.apify.task;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContextFactory;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@KestraTest
class RunTest {

    @Inject
    RunContextFactory runContextFactory;

    @Test
    void givenRequiredFields_whenBuildingTaskRun_thenBuildsSuccessfully() {
        var taskRun = assertDoesNotThrow(() -> Run.builder()
            .taskId(Property.ofValue("my_username~my-task"))
            .apiToken(Property.ofValue("fake-token"))
            .build());

        assertNotNull(taskRun.getTaskId());
    }

    @Test
    void givenTaskRunWithOptionalFields_whenBuildingTaskRun_thenBuildsSuccessfully() {
        var taskRun = assertDoesNotThrow(() -> Run.builder()
            .taskId(Property.ofValue("my_username~my-task"))
            .apiToken(Property.ofValue("fake-token"))
            .maxItems(Property.ofValue(100))
            .waitForFinish(Property.ofValue(30))
            .build());

        assertNotNull(taskRun.getTaskId());
        assertNotNull(taskRun.getMaxItems());
        assertNotNull(taskRun.getWaitForFinish());
    }
}
