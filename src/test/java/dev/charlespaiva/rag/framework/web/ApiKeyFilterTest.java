package dev.charlespaiva.rag.framework.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyFilterTest {

    @Test
    void shouldPassRequestWhenKeyMatches() throws Exception {
        // Given
        var filter = new ApiKeyFilter("secret-key");
        var request = new MockHttpServletRequest("POST", "/api/v1/documents");
        request.addHeader("X-Api-Key", "secret-key");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void shouldReturn401WhenKeyIsMissing() throws Exception {
        // Given
        var filter = new ApiKeyFilter("secret-key");
        var request = new MockHttpServletRequest("POST", "/api/v1/documents");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("X-Api-Key");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void shouldReturn401WhenKeyIsWrong() throws Exception {
        // Given
        var filter = new ApiKeyFilter("secret-key");
        var request = new MockHttpServletRequest("POST", "/api/v1/questions");
        request.addHeader("X-Api-Key", "wrong-key");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void shouldPassNonApiPathWithoutKeyCheck() throws Exception {
        // Given — actuator and Swagger must not require an API key
        var filter = new ApiKeyFilter("secret-key");
        var request = new MockHttpServletRequest("GET", "/actuator/health");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void shouldPassAllRequestsWhenKeyIsNotConfigured() throws Exception {
        // Given — empty key means authentication is disabled (local dev mode)
        var filter = new ApiKeyFilter("");
        var request = new MockHttpServletRequest("POST", "/api/v1/documents");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }
}
