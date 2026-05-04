package org.triplea.modules.user.account.login;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/** Fetches from database the lobby login message */
@ApplicationScoped
@AllArgsConstructor(onConstructor_ = @Inject)
public class LobbyLoginMessageDao implements Supplier<String> {

  private final Jdbi jdbi;

  static LobbyLoginMessageDao build(final Jdbi jdbi) {
    return new LobbyLoginMessageDao(jdbi);
  }

  @Override
  public String get() {
    return jdbi.withHandle(
            handle ->
                handle
                    .createQuery("select message from lobby_message") //
                    .mapTo(String.class)
                    .findOne())
        .orElse("");
  }
}
