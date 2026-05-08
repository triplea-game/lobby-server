package org.triplea.lobby.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import javax.annotation.Nullable;
import org.triplea.domain.data.ApiKey;

/**
 * Thin wrapper around Java's built-in {@link HttpClient} for use in integration tests. Handles JSON
 * serialization via Gson, sets required lobby headers, and throws {@link HttpStatusException} on
 * non-2xx responses.
 */
public class LobbyHttpClientHelper {

  private static final Gson GSON = new Gson();
  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
  private static final String SYSTEM_ID_HEADER = "System-Id-Header";
  private static final String TEST_SYSTEM_ID = "test-system-id";

  private final URI baseUri;
  @Nullable private final ApiKey apiKey;

  public LobbyHttpClientHelper(final URI baseUri, final ApiKey apiKey) {
    this.baseUri = baseUri;
    this.apiKey = apiKey;
  }

  public LobbyHttpClientHelper(final URI baseUri) {
    this(baseUri, null);
  }

  /** GET request returning a single typed object. */
  public <T> T get(final String path, final Class<T> responseType) {
    final HttpResponse<String> response = send(requestBuilder(path).GET().build());
    checkStatus(response);
    return GSON.fromJson(response.body(), responseType);
  }

  /** POST request with a JSON body, returning a typed list. */
  public <T> List<T> postForList(final String path, final Object body, final Class<T> elementType) {
    final Type listType = TypeToken.getParameterized(List.class, elementType).getType();
    return postInternal(path, body, listType);
  }

  /** POST request with a JSON body, returning a single typed object. */
  public <T> T post(final String path, final Object body, final Class<T> responseType) {
    return postInternal(path, body, responseType);
  }

  /** POST request with a JSON body, expecting no meaningful response body. */
  public void post(final String path, final Object body) {
    checkStatus(send(buildPostRequest(path, body)));
  }

  private <T> T postInternal(final String path, final Object body, final Type responseType) {
    final HttpResponse<String> response = send(buildPostRequest(path, body));
    checkStatus(response);
    return GSON.fromJson(response.body(), responseType);
  }

  private HttpRequest buildPostRequest(final String path, final Object body) {
    final String serialized = body instanceof String s ? s : GSON.toJson(body);
    return requestBuilder(path).POST(HttpRequest.BodyPublishers.ofString(serialized)).build();
  }

  private HttpRequest.Builder requestBuilder(final String path) {
    final HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUri.toString() + path))
            .header("Content-Type", "application/json")
            .header(SYSTEM_ID_HEADER, TEST_SYSTEM_ID);
    if (apiKey != null) {
      builder.header("Authorization", "Bearer " + apiKey.getValue());
    }
    return builder;
  }

  private HttpResponse<String> send(final HttpRequest request) {
    try {
      return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (final IOException | InterruptedException e) {
      throw new RuntimeException("HTTP request failed: " + request.uri(), e);
    }
  }

  private void checkStatus(final HttpResponse<String> response) {
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new HttpStatusException(response.statusCode(), response.body());
    }
  }
}
