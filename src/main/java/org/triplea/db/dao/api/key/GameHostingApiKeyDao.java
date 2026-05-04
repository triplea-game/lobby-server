package org.triplea.db.dao.api.key;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameHostingApiKeyDao {
  @Inject private final Jdbi jdbi;

  public int insertKey(String key, String ip) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into game_hosting_api_key(key, ip) values(:key, :ip::inet)
                    """)
                .bind("key", key)
                .bind("ip", ip)
                .execute());
  }

  boolean keyExists(String apiKey) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select exists (select * from game_hosting_api_key where key = :apiKey)
                    """)
                .bind("apiKey", apiKey)
                .mapTo(Boolean.class)
                .one());
  }
}
