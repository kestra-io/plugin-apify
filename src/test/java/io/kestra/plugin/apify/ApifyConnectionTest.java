package io.kestra.plugin.apify;

import io.kestra.core.http.HttpRequest;
import org.junit.jupiter.api.Test;

import static io.kestra.plugin.apify.ApifyConnection.INTEGRATION_HEADER;
import static io.kestra.plugin.apify.ApifyConnection.INTEGRATION_VALUE;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApifyConnectionTest {
    ApifyConnection apifyConnection = new ApifyConnection(){};

    @Test
    void givenGetRequest_whenBuilt_thenIncludesIntegrationPlatformHeader() {
        verifyHttpRequestIncludesPlatformHeader(apifyConnection.buildGetRequest("https://example.com"));
    }

    @Test
    void givenPostRequest_whenBuilt_thenIncludesIntegrationPlatformHeader() throws Exception {
        verifyHttpRequestIncludesPlatformHeader(apifyConnection.buildPostRequest("https://example.com", null));
    }

    @Test
    void givenPatchRequest_whenBuilt_thenIncludesIntegrationPlatformHeader() throws Exception {
        verifyHttpRequestIncludesPlatformHeader(apifyConnection.buildPatchRequest("https://example.com", null));
    }

    @Test
    void givenDeleteRequest_whenBuilt_thenIncludesIntegrationPlatformHeader() {
        verifyHttpRequestIncludesPlatformHeader(apifyConnection.buildDeleteRequest("https://example.com"));
    }

    private void verifyHttpRequestIncludesPlatformHeader(HttpRequest.HttpRequestBuilder httpRequest) {
        assertTrue(httpRequest.build().getHeaders().map().get(INTEGRATION_HEADER).contains(INTEGRATION_VALUE));
    }
}