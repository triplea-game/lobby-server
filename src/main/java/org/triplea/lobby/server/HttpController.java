package org.triplea.lobby.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.triplea.lobby.server.access.authentication.AuthenticatedUser;

/**
 * Base class for http server controllers. Provides shared JAX-RS annotations and a helper to
 * extract the authenticated user from the request security context.
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("RestResourceMethodInspection")
public class HttpController {

  /** Returns the authenticated user principal carried in this request's security context. */
  protected AuthenticatedUser user(final SecurityContext sc) {
    return (AuthenticatedUser) sc.getUserPrincipal();
  }
}
