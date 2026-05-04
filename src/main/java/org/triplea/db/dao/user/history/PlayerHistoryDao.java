package org.triplea.db.dao.user.history;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/** DAO to look up a players stats. Intended to be used when getting player information. */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerHistoryDao {
  private final Jdbi jdbi;

  public Optional<PlayerHistoryRecord> lookupPlayerHistoryByUserId(int userId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        date_created as date_registered
                      from lobby_user
                      where id = :userId
                    """)
                .bind("userId", userId)
                .map(ConstructorMapper.of(PlayerHistoryRecord.class))
                .findOne());
  }
}
