package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.words.ToolboxBadWordsClient;
import org.triplea.modules.moderation.bad.words.BadWordsService;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;

/** Controller for servicing moderator toolbox bad-words tab (provides CRUD operations). */
@Builder
@RolesAllowed(UserRole.MODERATOR)
public class BadWordsController extends HttpController {
  @Nonnull private final BadWordsService badWordsService;

  public static BadWordsController build(final Jdbi jdbi) {
    return BadWordsController.builder() //
        .badWordsService(BadWordsService.build(jdbi))
        .build();
  }

  /**
   * Removes a bad word entry from the bad-word table.
   *
   * @param word The new bad word entry to remove. We expect this to exist in the table or else we
   *     return a 400.
   */
  @POST
  @Path(ToolboxBadWordsClient.BAD_WORD_REMOVE_PATH)
  public Response removeBadWord(
      @Auth final AuthenticatedUser authenticatedUser, final String word) {
    Preconditions.checkArgument(word != null && !word.isEmpty());
    badWordsService.removeBadWord(authenticatedUser.getUserIdOrThrow(), word);
    return Response.ok().entity("Removed bad word: " + word).build();
  }

  /**
   * Adds a bad word entry to the bad-word table.
   *
   * @param word The new bad word entry to add.
   */
  @POST
  @Path(ToolboxBadWordsClient.BAD_WORD_ADD_PATH)
  public Response addBadWord(@Auth final AuthenticatedUser authenticatedUser, final String word) {
    Preconditions.checkArgument(word != null && !word.isEmpty());
    return badWordsService.addBadWord(authenticatedUser.getUserIdOrThrow(), word)
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
