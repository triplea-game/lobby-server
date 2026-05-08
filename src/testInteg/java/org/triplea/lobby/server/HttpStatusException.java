package org.triplea.lobby.server;

/** Thrown when an HTTP response has a non-2xx status code. */
public class HttpStatusException extends RuntimeException {
  private final int status;

  public HttpStatusException(final int status, final String body) {
    super("HTTP " + status + ": " + body);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
