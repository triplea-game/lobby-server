package org.triplea.lobby.server.controllers.lobby.moderation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.words.ToolboxBadWordsClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.moderation.bad.words.BadWordsService;

/** Controller for servicing moderator toolbox bad-words tab (provides CRUD operations). */
@ApplicationScoped
@RolesAllowed(UserRole.MODERATOR)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BadWordsController extends HttpController {

  @Inject Jdbi jdbi;

  private BadWordsService badWordsService;

  @PostConstruct
  void init() {
    badWordsService = BadWordsService.build(jdbi);
  }

  /**
   * Removes a bad word entry from the bad-word table.
   *
   * @param word The new bad word entry to remove. We expect this to exist in the table or else we
   *     return a 400.
   */
  @POST
  @Path(ToolboxBadWordsClient.BAD_WORD_REMOVE_PATH)
  public Response removeBadWord(@Context final SecurityContext sc, final String word) {
    if (word == null || word.isEmpty()) {
      throw new jakarta.ws.rs.BadRequestException("Word cannot be null or empty");
    }
    badWordsService.removeBadWord(user(sc).getUserIdOrThrow(), word);
    return Response.ok().entity("Removed bad word: " + word).build();
  }

  /**
   * Adds a bad word entry to the bad-word table.
   *
   * @param word The new bad word entry to add.
   */
  @POST
  @Path(ToolboxBadWordsClient.BAD_WORD_ADD_PATH)
  public Response addBadWord(@Context final SecurityContext sc, final String word) {
    if (word == null || word.isEmpty()) {
      throw new jakarta.ws.rs.BadRequestException("Word cannot be null or empty");
    }
    return badWordsService.addBadWord(user(sc).getUserIdOrThrow(), word)
        ? Response.ok().build()
        : Response.status(400)
            .entity(word + " was not added, it may already have been added")
            .build();
  }

  @GET
  @Path(ToolboxBadWordsClient.BAD_WORD_GET_PATH)
  public Response getBadWords() {
    return Response.status(200).entity(badWordsService.getBadWords()).build();
  }
}
