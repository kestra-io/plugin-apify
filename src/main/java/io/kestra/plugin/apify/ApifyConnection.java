package io.kestra.plugin.apify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.apify.client.ApifyClient;
import com.apify.client.ApifyClientBuilder;
import com.apify.client.http.DefaultHttpTransport;
import com.apify.client.http.HttpTransport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.http.client.configurations.HttpConfiguration;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Make authenticated Apify API calls",
    description = "Base class for Apify tasks that signs requests with the apiToken, sets the Apify integration header, and honours the optional apify.api.base.url system override (default https://api.apify.com/v2)."
)
@Plugin()
public abstract class ApifyConnection extends Task implements ApifyConnectionInterface {
    protected final static ObjectMapper mapper = JacksonMapper.ofJson(false);
    private static final String APIFY_API_URL = "https://api.apify.com/v2";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    protected static final String INTEGRATION_VALUE = "kestra";
    protected static final String INTEGRATION_HEADER = "x-apify-integration-platform";

    @NotNull
    @ToString.Exclude
    private Property<String> apiToken;

    @Schema(
        title = "HTTP client options",
        description = "Optional HttpConfiguration applied to every Apify call. SDK-backed tasks honour `timeout.readIdleTimeout` only, the raw HTTP tasks honour the full configuration."
    )
    HttpConfiguration options;

    protected static String getBaseUrl() {
        String overrideUrl = System.getProperty("apify.api.base.url");
        return overrideUrl != null ? overrideUrl : APIFY_API_URL;
    }

    protected String addQueryParams(String basePath, Map<String, ?> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(basePath);
        Map<String, ?> sortedQueryParams = new TreeMap<>(queryParams);
        sortedQueryParams.forEach((key, value) ->
        {
            urlBuilder.append(urlBuilder.indexOf("?") == -1 ? "?" : "&");
            urlBuilder.append(key).append("=").append(encodeValue(value.toString()));
        });
        return urlBuilder.toString();
    }

    /**
     * Makes an HTTP call to the Apify API with proper error handling
     */
    public <T> T makeCall(RunContext runContext, HttpRequest.HttpRequestBuilder requestBuilder, Class<T> responseType) throws Exception {
        var logger = runContext.logger();

        try (HttpClient client = new HttpClient(runContext, options)) {
            addAuthorizationHeader(runContext, requestBuilder);
            HttpResponse<T> response = client.request(requestBuilder.build(), responseType);
            return response.getBody();
        } catch (IllegalVariableEvaluationException illegalVariableEvaluationException) {
            logger.error("Error getting API key for Apify: {}", illegalVariableEvaluationException.getMessage());
            throw illegalVariableEvaluationException;
        } catch (Exception e) {
            logger.error("Error making request to Apify API: {}", e.getMessage());
            throw e;
        }
    }

    public URI makeCallAndWriteToFile(RunContext runContext, HttpRequest.HttpRequestBuilder requestBuilder) throws Exception {
        var logger = runContext.logger();
        addAuthorizationHeader(runContext, requestBuilder);
        CompletableFuture<URI> completableFuture = new CompletableFuture<>();
        try (HttpClient client = new HttpClient(runContext, options)) {
            client.request(requestBuilder.build(), getWriteHttpResponseToTempFileConsumer(runContext, completableFuture));
        } catch (IllegalVariableEvaluationException illegalVariableEvaluationException) {
            logger.error("Error getting API key for Apify: {}", illegalVariableEvaluationException.getMessage());
            completableFuture.completeExceptionally(illegalVariableEvaluationException);
        } catch (Exception e) {
            if (e.getClass().equals(ApifyTempFileRuntimeException.class)) {
                logger.error("Error saving Apify Response to local temp file: {}", e.getCause().getMessage());
            } else {
                logger.error("Error making request to Apify API: {}", e.getMessage());
            }
            completableFuture.completeExceptionally(e);
        }
        return completableFuture.get();
    }

    private static Consumer<HttpResponse<InputStream>> getWriteHttpResponseToTempFileConsumer(RunContext runContext, CompletableFuture<URI> completableFuture) {
        return (HttpResponse<InputStream> response) ->
        {
            if (response.getStatus().getCode() != 200) {
                completableFuture.completeExceptionally(new Exception("Received non-200 response from Apify API: " + response.getStatus().getCode()));
                return;
            }
            try {
                completableFuture.complete(runContext.storage().putFile(response.getBody(), UUID.randomUUID().toString()));
            } catch (IOException e) {
                completableFuture.completeExceptionally(new ApifyTempFileRuntimeException(e));
            }
        };
    }

    /**
     * Creates a GET request builder with authentication and headers
     */
    protected HttpRequest.HttpRequestBuilder buildGetRequest(String url) {
        return getBaseHttpRequestBuilder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("GET");
    }

    /**
     * Creates a POST request builder with authentication, headers, and JSON body
     */
    protected HttpRequest.HttpRequestBuilder buildPostRequest(String url, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);

        return getBaseHttpRequestBuilder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("POST")
            .body(HttpRequest.StringRequestBody.builder().content(jsonBody).build());
    }

    /**
     * Creates a PATCH request builder with authentication, headers, and JSON body
     */
    protected HttpRequest.HttpRequestBuilder buildPatchRequest(String url, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);

        return getBaseHttpRequestBuilder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("PATCH")
            .body(HttpRequest.StringRequestBody.builder().content(jsonBody).build());
    }

    /**
     * Creates a DELETE request builder with authentication and headers
     */
    protected HttpRequest.HttpRequestBuilder buildDeleteRequest(String url) {
        return getBaseHttpRequestBuilder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("DELETE");
    }

    private static HttpRequest.HttpRequestBuilder getBaseHttpRequestBuilder() {
        return HttpRequest.builder().addHeader(INTEGRATION_HEADER, INTEGRATION_VALUE);
    }

    /**
     * Adds authentication and required headers to the HTTP request
     */
    private void addAuthorizationHeader(
        RunContext runContext,
        HttpRequest.HttpRequestBuilder requestBuilder) throws IllegalVariableEvaluationException {

        if (
            requestBuilder.build().getHeaders() != null
                && requestBuilder.build().getHeaders().map().containsKey("Authorization")
        ) {
            return;
        }

        String apiTokenRendered = runContext.render(this.apiToken).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("Missing required apiToken field")
        );

        requestBuilder
            .addHeader("Authorization", "Bearer " + apiTokenRendered)
            .addHeader("Content-Type", JSON_CONTENT_TYPE);
    }

    /**
     * Builds an SDK client. The integration header is re-added through a transport wrapper because the SDK has no
     * header hook, and Apify uses it to attribute traffic to Kestra.
     */
    protected ApifyClient apifyClient(RunContext runContext) throws IllegalVariableEvaluationException {
        String rApiToken = runContext.render(this.apiToken).as(String.class).orElseThrow(
            () -> new IllegalArgumentException("Missing required apiToken field")
        );

        ApifyClientBuilder builder = ApifyClient.builder()
            .token(rApiToken)
            .baseUrl(sdkBaseUrl())
            .httpTransport(new IntegrationHeaderTransport(new DefaultHttpTransport()));

        if (this.options != null && this.options.getTimeout() != null) {
            runContext.render(this.options.getTimeout().getReadIdleTimeout())
                .as(Duration.class)
                .ifPresent(builder::timeout);
        }

        return builder.build();
    }

    /** The SDK appends the /v2 API prefix itself, our own base URL already carries it. */
    private static String sdkBaseUrl() {
        String base = getBaseUrl();
        return base.endsWith("/v2") ? base.substring(0, base.length() - 3) : base;
    }

    /** Stamps every SDK request with the integration header the raw HTTP path used to send. */
    private record IntegrationHeaderTransport(HttpTransport delegate) implements HttpTransport {
        @Override
        public CompletableFuture<java.net.http.HttpResponse<byte[]>> sendAsync(java.net.http.HttpRequest request) {
            return delegate.sendAsync(withHeader(request));
        }

        @Override
        public CompletableFuture<java.net.http.HttpResponse<InputStream>> sendStreamingAsync(java.net.http.HttpRequest request) {
            return delegate.sendStreamingAsync(withHeader(request));
        }

        private static java.net.http.HttpRequest withHeader(java.net.http.HttpRequest request) {
            return java.net.http.HttpRequest.newBuilder(request, (name, value) -> true)
                .header(INTEGRATION_HEADER, INTEGRATION_VALUE)
                .build();
        }
    }

    /**
     * Re-reads an SDK model as one of our own so task outputs keep the exact shape they had before the SDK.
     * The Instant serializer preserves millisecond precision, which the SDK's default drops.
     */
    protected static <T> T asPluginModel(Object sdkModel, Class<T> type) throws Exception {
        return mapper.readValue(SDK_MAPPER.writeValueAsString(sdkModel), type);
    }

    private static final tools.jackson.databind.json.JsonMapper SDK_MAPPER = tools.jackson.databind.json.JsonMapper
        .builder()
        .addModule(
            new tools.jackson.databind.module.SimpleModule().addSerializer(
                Instant.class,
                new tools.jackson.databind.ser.std.StdSerializer<Instant>(Instant.class) {
                    @Override
                    public void serialize(Instant value, tools.jackson.core.JsonGenerator gen, tools.jackson.databind.SerializationContext ctx) {
                        gen.writeString(MILLIS_UTC.format(value));
                    }
                }
            )
        )
        .build();

    private static final DateTimeFormatter MILLIS_UTC = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneOffset.UTC);

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Both run tasks take the same base64 payload, and a bad one has to name the field rather than leak a parse error. */
    protected static Optional<List<Object>> decodedWebhooks(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                mapper.readValue(Base64.getDecoder().decode(encoded), new TypeReference<>() {
                })
            );
        } catch (IllegalArgumentException | java.io.IOException e) {
            throw new IllegalArgumentException("webhooks is not valid base64 encoded JSON: %s".formatted(e.getMessage()), e);
        }
    }
}
