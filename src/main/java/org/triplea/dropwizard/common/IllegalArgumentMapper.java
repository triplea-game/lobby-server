package org.triplea.dropwizard.common;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * This class is used to convert IllegalArgumentExceptions thrown by http endpoint controllers to
 * return HTTP status 400 codes. Without this, those errors would be 500s.
 */
public class IllegalArgumentMapper implements ExceptionMapper<IllegalArgumentException> {
  @Override
  public Response toResponse(final IllegalArgumentException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity("Http 400 - Bad Request: " + exception.getMessage())
        .build();
  }
}
