package org.triplea.lobby.server.access.authentication;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Set;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;

/**
 * Validates a bearer-token API key and builds a {@link SecurityIdentity} carrying an {@link
 * AuthenticatedUser} as its principal. The validated identity is cached per token for ten minutes
 * to avoid a database round-trip on every request.
 *
 * <p>The role hierarchy (ADMIN ⊇ MODERATOR ⊇ PLAYER ⊇ ANONYMOUS) is encoded by adding all implied
 * roles to the identity so that {@code @RolesAllowed} checks on lower-privilege endpoints pass
 * automatically for higher-privilege users.
 */
@ApplicationScoped
public class ApiKeyIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

  @Inject Jdbi jdbi;

  private ApiKeyAuthenticator authenticator;
  private Cache<String, SecurityIdentity> cache;

  /** CDI no-arg constructor. */
  ApiKeyIdentityProvider() {}

  @PostConstruct
  void init() {
    authenticator = ApiKeyAuthenticator.build(jdbi);
    cache =
        Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(10_000).build();
  }

  @Override
  public Class<TokenAuthenticationRequest> getRequestType() {
    return TokenAuthenticationRequest.class;
  }

  @Override
  public Uni<SecurityIdentity> authenticate(
      final TokenAuthenticationRequest request, final AuthenticationRequestContext context) {
    final String token = request.getToken().getToken();
    final SecurityIdentity cached = cache.getIfPresent(token);
    if (cached != null) {
      return Uni.createFrom().item(cached);
    }
    return context.runBlocking(
        () -> {
          final SecurityIdentity identity =
              authenticator
                  .authenticate(token)
                  .map(user -> buildIdentity(user, token))
                  .orElse(null);
          if (identity != null) {
            cache.put(token, identity);
          }
          return identity;
        });
  }

  private static SecurityIdentity buildIdentity(final AuthenticatedUser user, final String token) {
    return QuarkusSecurityIdentity.builder()
        .setPrincipal(user)
        .addRoles(rolesFor(user.getUserRole()))
        .addCredential(new io.quarkus.security.credential.TokenCredential(token, "Bearer"))
        .build();
  }

  /**
   * Returns all roles that cover the given user role, respecting the hierarchy ADMIN ⊇ MODERATOR ⊇
   * PLAYER ⊇ ANONYMOUS. HOST is a peer role not in this hierarchy.
   */
  private static Set<String> rolesFor(final String userRole) {
    switch (userRole) {
      case UserRole.ADMIN:
        return Set.of(UserRole.ADMIN, UserRole.MODERATOR, UserRole.PLAYER, UserRole.ANONYMOUS);
      case UserRole.MODERATOR:
        return Set.of(UserRole.MODERATOR, UserRole.PLAYER, UserRole.ANONYMOUS);
      case UserRole.PLAYER:
        return Set.of(UserRole.PLAYER, UserRole.ANONYMOUS);
      case UserRole.ANONYMOUS:
        return Set.of(UserRole.ANONYMOUS);
      case UserRole.HOST:
        return Set.of(UserRole.HOST);
      default:
        throw new AssertionError("Unrecognised user role: " + userRole);
    }
  }
}
