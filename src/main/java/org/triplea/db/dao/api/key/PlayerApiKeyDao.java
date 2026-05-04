package org.triplea.db.dao.api.key;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/**
 * Dao for interacting with api_key table. Api_key table stores keys that are generated on login.
 * For non-anonymous accounts, the key is linked back to the players account which is used to
 * determine the users 'Role'. Anonymous users are still granted API keys, they have no user id and
 * given role 'ANONYMOUS'.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerApiKeyDao {
  private final Jdbi jdbi;

  public int storeKey(
      String username,
      @Nullable Integer userId,
      int userRoleId,
      String playerChatId,
      String key,
      String systemId,
      String ipAddress) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into lobby_api_key(
                       username, lobby_user_id, user_role_id, player_chat_id, key, system_id, ip)
                    values
                    (:username, :userId, :userRoleId, :playerChatId, :apiKey, :systemId, :ip::inet)
                    """)
                .bind("username", username)
                .bind("userId", userId)
                .bind("userRoleId", userRoleId)
                .bind("playerChatId", playerChatId)
                .bind("apiKey", key)
                .bind("systemId", systemId)
                .bind("ip", ipAddress)
                .execute());
  }

  public Optional<PlayerApiKeyLookupRecord> lookupByApiKey(String apiKey) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        ak.lobby_user_id as user_id,
                        ak.id as api_key_id,
                        ak.username as username,
                        ur.name as user_role,
                        ak.player_chat_id as player_chat_id
                    from lobby_api_key ak
                    join user_role ur on ur.id = ak.user_role_id
                    left join lobby_user lu on lu.id = ak.lobby_user_id
                    where ak.key = :apiKey
                    """)
                .bind("apiKey", apiKey)
                .map(ConstructorMapper.of(PlayerApiKeyLookupRecord.class))
                .findOne());
  }

  public void deleteOldKeys() {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    delete from lobby_api_key where date_created < (now() - '7 days'::interval)
                    """)
                .execute());
  }

  public Optional<PlayerIdentifiersByApiKeyLookup> lookupByPlayerChatId(String playerChatId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        username,
                        system_id,
                        ip
                    from lobby_api_key
                    where player_chat_id = :playerChatId
                    """)
                .bind("playerChatId", playerChatId)
                .map(ConstructorMapper.of(PlayerIdentifiersByApiKeyLookup.class))
                .findOne());
  }

  public Optional<Integer> lookupPlayerIdByPlayerChatId(String playerChatId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        lobby_user_id
                    from lobby_api_key
                    where player_chat_id = :playerChatId
                    """)
                .bind("playerChatId", playerChatId)
                .mapTo(Integer.class)
                .findOne());
  }
}
