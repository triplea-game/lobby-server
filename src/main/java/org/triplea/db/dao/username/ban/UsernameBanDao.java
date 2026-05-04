package org.triplea.db.dao.username.ban;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/** Interface with the banned_names table, these are exact match names not allowed in the lobby. */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UsernameBanDao {
  private final Jdbi jdbi;

  public List<UsernameBanRecord> getBannedUserNames() {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        username,
                        date_created
                      from banned_username
                      order by username asc
                    """)
                .map(ConstructorMapper.of(UsernameBanRecord.class))
                .list());
  }

  public int addBannedUserName(String nameToBan) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into banned_username(username)
                    values(:nameToBan)
                    on conflict(username) do nothing
                    """)
                .bind("nameToBan", nameToBan)
                .execute());
  }

  public int removeBannedUserName(String nameToRemove) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    delete from banned_username where username = :nameToRemove
                    """)
                .bind("nameToRemove", nameToRemove)
                .execute());
  }

  public boolean nameIsBanned(String playerName) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select exists (
                    select *
                    from banned_username
                    where username = upper(:playerName))
                    """)
                .bind("playerName", playerName)
                .mapTo(Boolean.class)
                .one());
  }
}
