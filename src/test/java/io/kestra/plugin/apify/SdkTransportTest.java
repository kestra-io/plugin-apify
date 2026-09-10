package io.kestra.plugin.apify;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.apify.actor.ActorRun;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/** Exercises the real transport against a stub, which the pre-existing tests never did. */
@KestraTest
class SdkTransportTest {
    private static final String RUN_JSON = """
        {"data":{"id":"run1","actId":"act1","userId":"u1","status":"SUCCEEDED","statusMessage":"done",
         "startedAt":"2024-01-01T00:00:00.000Z","finishedAt":"2024-01-01T00:05:00.000Z",
         "buildId":"b1","buildNumber":"0.1.2","exitCode":0,"containerUrl":"https://c",
         "defaultDatasetId":"d1","defaultKeyValueStoreId":"k1","defaultRequestQueueId":"q1",
         "generalAccess":"FOLLOW_USER_SETTING","usageTotalUsd":0.031,"chargedEventCounts":{"page":3}}}
        """;

    @Inject
    private RunContextFactory runContextFactory;

    private HttpServer server;
    private final List<String> requests = new ArrayList<>();
    private final Map<String, String> headers = new java.util.HashMap<>();

    @BeforeEach
    void start() throws IOException {
        requests.clear();
        headers.clear();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange ->
        {
            requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI());
            exchange.getRequestHeaders().forEach((n, v) -> headers.put(n.toLowerCase(), v.get(0)));
            respond(exchange, RUN_JSON);
        });
        server.start();
        System.setProperty("apify.api.base.url", "http://localhost:" + server.getAddress().getPort() + "/v2");
    }

    @AfterEach
    void stop() {
        server.stop(0);
        System.clearProperty("apify.api.base.url");
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @Test
    void actorRunHitsTheRightEndpointAndKeepsItsOutputShape() throws Exception {
        var task = io.kestra.plugin.apify.actor.Run.builder()
            .apiToken(Property.ofValue("t0ken"))
            .actorId(Property.ofValue("act1"))
            .build();

        ActorRun out = task.run(runContextFactory.of());

        assertThat(requests.getFirst(), containsString("POST /v2/actors/act1/runs"));
        assertThat(headers.get("authorization"), is("Bearer t0ken"));
        assertThat(headers.get("x-apify-integration-platform"), is("kestra"));

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
        var task = io.kestra.plugin.apify.dataset.GetLastRun.builder()
            .apiToken(Property.ofValue("t0ken"))
            .actorId(Property.ofValue("act1"))
            .build();

        ActorRun out = task.run(runContextFactory.of());

        assertThat(requests.getFirst(), containsString("/v2/actors/act1/runs/last"));
        assertThat(out.getId(), is("run1"));
    }

    @Test
    void taskRunHitsTheRightEndpoint() throws Exception {
        var task = io.kestra.plugin.apify.task.Run.builder()
            .apiToken(Property.ofValue("t0ken"))
            .taskId(Property.ofValue("task1"))
            .build();

        ActorRun out = task.run(runContextFactory.of());

        assertThat(requests.getFirst(), containsString("POST /v2/actor-tasks/task1/runs"));
        assertThat(out.getId(), is("run1"));
    }
}
