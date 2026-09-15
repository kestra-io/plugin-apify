package io.kestra.plugin.apify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.apify.actor.ActorRun;

import jakarta.inject.Inject;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Exercises the real transport against a stub, which the pre-existing tests never did. */
@KestraTest
@WireMockTest(httpPort = SdkTransportTest.STUB_PORT)
class SdkTransportTest {
    static final int STUB_PORT = 28182;

    private static final String RUN_JSON = """
        {"data":{"id":"run1","actId":"act1","userId":"u1","status":"SUCCEEDED","statusMessage":"done",
         "startedAt":"2024-01-01T00:00:00.000Z","finishedAt":"2024-01-01T00:05:00.000Z",
         "buildId":"b1","buildNumber":"0.1.2","exitCode":0,"containerUrl":"https://c",
         "defaultDatasetId":"d1","defaultKeyValueStoreId":"k1","defaultRequestQueueId":"q1",
         "generalAccess":"FOLLOW_USER_SETTING","usageTotalUsd":0.031,"chargedEventCounts":{"page":3}}}
        """;

    @Inject
    private RunContextFactory runContextFactory;

    /** apify.api.base.url is the production override the client already reads, and the only seam for the stub. */
    @BeforeAll
    static void pointClientAtStub() {
        System.setProperty("apify.api.base.url", "http://localhost:" + STUB_PORT + "/v2");
    }

    @AfterAll
    static void clearBaseUrl() {
        System.clearProperty("apify.api.base.url");
    }

    @BeforeEach
    void stubApi() {
        stubFor(post(urlPathEqualTo("/v2/actors/act1/runs")).willReturn(okJson(RUN_JSON)));
        stubFor(get(urlPathEqualTo("/v2/actors/act1/runs/last")).willReturn(okJson(RUN_JSON)));
        stubFor(post(urlPathEqualTo("/v2/actor-tasks/task1/runs")).willReturn(okJson(RUN_JSON)));
    }

    @Test
    void actorRunHitsTheRightEndpointAndKeepsItsOutputShape() throws Exception {
        ActorRun out = io.kestra.plugin.apify.actor.Run.builder()
            .apiToken(Property.ofValue("t0ken"))
            .actorId(Property.ofValue("act1"))
            .build()
            .run(runContextFactory.of());

        verify(
            postRequestedFor(urlPathEqualTo("/v2/actors/act1/runs"))
                .withHeader("Authorization", equalTo("Bearer t0ken"))
                .withHeader("x-apify-integration-platform", equalTo("kestra"))
        );

        assertThat(out, notNullValue());
        // the exact strings the raw HTTP implementation produced, including the coerced types
        String json = JacksonMapper.ofJson().writeValueAsString(out);
        assertThat(json, containsString("\"startedAt\":\"2024-01-01T00:00:00.000Z\""));
        assertThat(json, containsString("\"usageTotalUsd\":\"0.031\""));
        assertThat(json, containsString("\"exitCode\":0.0"));
        assertThat(json, containsString("\"chargedEventCounts\":{\"page\":3.0}"));
        assertThat(json, containsString("\"generalAccess\":\"FOLLOW_USER_SETTING\""));
    }

    @Test
    void getLastRunHitsTheRightEndpoint() throws Exception {
        ActorRun out = io.kestra.plugin.apify.dataset.GetLastRun.builder()
            .apiToken(Property.ofValue("t0ken"))
            .actorId(Property.ofValue("act1"))
            .build()
            .run(runContextFactory.of());

        verify(getRequestedFor(urlPathEqualTo("/v2/actors/act1/runs/last")));
        assertThat(out.getId(), is("run1"));
    }

    @Test
    void taskRunHitsTheRightEndpoint() throws Exception {
        ActorRun out = io.kestra.plugin.apify.task.Run.builder()
            .apiToken(Property.ofValue("t0ken"))
            .taskId(Property.ofValue("task1"))
            .build()
            .run(runContextFactory.of());

        verify(postRequestedFor(urlPathEqualTo("/v2/actor-tasks/task1/runs")));
        assertThat(out.getId(), is("run1"));
    }

    @Test
    void reportsAnEmptyLastRunInsteadOfFailingOnNull() {
        stubFor(get(urlPathEqualTo("/v2/actors/act1/runs/last")).willReturn(okJson("{\"data\":null}")));

        var task = io.kestra.plugin.apify.dataset.GetLastRun.builder()
            .apiToken(Property.ofValue("t0ken"))
            .actorId(Property.ofValue("act1"))
            .build();

        var exception = assertThrows(IllegalStateException.class, () -> task.run(runContextFactory.of()));
        assertThat(exception.getMessage(), containsString("act1"));
    }

    @Test
    void namesTheWebhooksFieldWhenItIsNotValidBase64() {
        var task = io.kestra.plugin.apify.actor.Run.builder()
            .apiToken(Property.ofValue("t0ken"))
            .actorId(Property.ofValue("act1"))
            .webhooks(Property.ofValue("not-base64!!"))
            .build();

        var exception = assertThrows(IllegalArgumentException.class, () -> task.run(runContextFactory.of()));
        assertThat(exception.getMessage(), containsString("webhooks"));
    }
}
