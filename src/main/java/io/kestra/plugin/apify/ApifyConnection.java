package io.kestra.plugin.apify;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Run Actor",
    description = "Run Actor for given Actor ID"
)
@Plugin()
public abstract class ApifyConnection extends Task implements ApifyConnectionInterface {
    protected final static ObjectMapper mapper = JacksonMapper.ofJson(false);
    private static final String APIFY_API_URL = "https://api.apify.com/v2";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

    private Property<String> apiToken;

    @Schema(title = "The HTTP client configuration.")
    HttpConfiguration options;

    protected static String getBaseUrl() {
        String overrideUrl = System.getProperty("apify.api.base.url");
        return overrideUrl != null ? overrideUrl : APIFY_API_URL;
    }

    protected String addQueryParams(String basePath, Map<String, ?> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(basePath);
        Map<String, ?> sortedQueryParams = new TreeMap<>(queryParams);
        sortedQueryParams.forEach((key, value) -> {
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
        return (HttpResponse<InputStream> response) -> {
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
        return HttpRequest.builder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("GET");
    }

    /**
     * Creates a POST request builder with authentication, headers, and JSON body
     */
    protected HttpRequest.HttpRequestBuilder buildPostRequest(String url, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);

        return HttpRequest.builder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("POST")
            .body(HttpRequest.StringRequestBody.builder().content(jsonBody).build());
    }

    /**
     * Creates a PATCH request builder with authentication, headers, and JSON body
     */
    protected HttpRequest.HttpRequestBuilder buildPatchRequest(String url, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);

        return HttpRequest.builder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("PATCH")
            .body(HttpRequest.StringRequestBody.builder().content(jsonBody).build());
    }

    /**
     * Creates a DELETE request builder with authentication and headers
     */
    protected HttpRequest.HttpRequestBuilder buildDeleteRequest(String url)  {
        return HttpRequest.builder()
            .uri(URI.create(getBaseUrl() + "/" + url))
            .method("DELETE");
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

        Optional<String> apiTokenRendered = runContext.render(this.apiToken).as(String.class);

        if (apiTokenRendered.isEmpty()) {
            throw new IllegalArgumentException("Missing required apiToken field");
        }

        requestBuilder
            .addHeader("Authorization", "Bearer " + apiTokenRendered.get())
            .addHeader("Content-Type", JSON_CONTENT_TYPE);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
