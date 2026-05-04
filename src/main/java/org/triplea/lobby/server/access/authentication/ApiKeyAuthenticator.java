package org.triplea.lobby.server.access.authentication;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.api.key.GameHostingApiKeyDaoWrapper;
import org.triplea.db.dao.api.key.PlayerApiKeyDaoWrapper;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.ApiKey;

/**
 * Validates a bearer-token API key. Returns an {@link AuthenticatedUser} when the key is found in
 * the database, or empty when the key is unknown. Anonymous users carry a null user-id and the
 * {@code ANONYMOUS} role.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @VisibleForTesting)
public class ApiKeyAuthenticator {

  private final PlayerApiKeyDaoWrapper apiKeyDaoWrapper;
  private final GameHostingApiKeyDaoWrapper gameHostingApiKeyDaoWrapper;

  public static ApiKeyAuthenticator build(final Jdbi jdbi) {
    return new ApiKeyAuthenticator(
        PlayerApiKeyDaoWrapper.build(jdbi), GameHostingApiKeyDaoWrapper.build(jdbi));
  }

  public Optional<AuthenticatedUser> authenticate(final String apiKey) {
    final ApiKey key = ApiKey.of(apiKey);
    return apiKeyDaoWrapper
        .lookupByApiKey(key)
        .map(
            userData ->
                AuthenticatedUser.builder()
                    .userId(userData.getUserId())
                    .name(userData.getUsername())
                    .userRole(userData.getUserRole())
                    .apiKey(key)
                    .build())
        .or(
            () ->
                gameHostingApiKeyDaoWrapper.isKeyValid(key)
                    ? Optional.of(
                        AuthenticatedUser.builder().userRole(UserRole.HOST).apiKey(key).build())
                    : Optional.empty());
  }
}
