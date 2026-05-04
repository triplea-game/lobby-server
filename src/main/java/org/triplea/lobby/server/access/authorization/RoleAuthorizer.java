package org.triplea.lobby.server.access.authorization;

import org.triplea.db.dao.user.role.UserRole;
import org.triplea.lobby.server.access.authentication.AuthenticatedUser;

/**
 * Answers whether an authenticated user is authorised to assume a requested role. The role
 * hierarchy is: ADMIN &gt; MODERATOR &gt; PLAYER &gt; ANONYMOUS. HOST is a peer role used only for
 * game-hosting connections.
 */
public class RoleAuthorizer {

  public boolean authorize(final AuthenticatedUser user, final String requestedRole) {
    switch (user.getUserRole()) {
      case UserRole.ADMIN:
        return adminAuthorizedFor(requestedRole);
      case UserRole.MODERATOR:
        return moderatorAuthorizedFor(requestedRole);
      case UserRole.PLAYER:
        return playerAuthorizedFor(requestedRole);
      case UserRole.ANONYMOUS:
        return anonymousAuthorizedFor(requestedRole);
      case UserRole.HOST:
        return requestedRole.equals(UserRole.HOST);
      default:
        throw new AssertionError("Unrecognized user role: " + user.getUserRole());
    }
  }

  private static boolean adminAuthorizedFor(final String requestedRole) {
    return requestedRole.equals(UserRole.ADMIN) || moderatorAuthorizedFor(requestedRole);
  }

  private static boolean moderatorAuthorizedFor(final String requestedRole) {
    return requestedRole.equals(UserRole.MODERATOR) || playerAuthorizedFor(requestedRole);
  }

  private static boolean playerAuthorizedFor(final String requestedRole) {
    return requestedRole.equals(UserRole.PLAYER) || anonymousAuthorizedFor(requestedRole);
  }

  private static boolean anonymousAuthorizedFor(final String requestedRole) {
    return requestedRole.equals(UserRole.ANONYMOUS);
  }
}
