package org.triplea.lobby.server.access.authentication;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.Collections;
import java.util.Set;

/**
 * Extracts the bearer token from the {@code Authorization} header and delegates validation to
 * {@link ApiKeyIdentityProvider}. Requests without an {@code Authorization} header are left
 * unauthenticated so that public endpoints (login, create-account, etc.) remain accessible.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class ApiKeyAuthenticationMechanism implements HttpAuthenticationMechanism {

  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public Uni<SecurityIdentity> authenticate(
      final RoutingContext context, final IdentityProviderManager identityProviderManager) {
    final String authHeader = context.request().getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return Uni.createFrom().optional(java.util.Optional.empty());
    }
    final String token = authHeader.substring(BEARER_PREFIX.length()).trim();
    return identityProviderManager.authenticate(
        new TokenAuthenticationRequest(new TokenCredential(token, "Bearer")));
  }

  @Override
  public Uni<ChallengeData> getChallenge(final RoutingContext context) {
    return Uni.createFrom()
        .item(new ChallengeData(401, "WWW-Authenticate", "Bearer realm=\"lobby\""));
  }

  @Override
  public Set<Class<? extends io.quarkus.security.identity.request.AuthenticationRequest>>
      getCredentialTypes() {
    return Collections.singleton(TokenAuthenticationRequest.class);
  }
}
