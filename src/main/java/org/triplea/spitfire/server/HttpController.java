package org.triplea.spitfire.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Base class for http server controllers. This class is mainly to share the annotations needed for
 * enabling an http controller. All http controller classes should be 'registered' in the
 * application configuration, {@see ServerApplication}
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("RestResourceMethodInspection")
public class HttpController {}
