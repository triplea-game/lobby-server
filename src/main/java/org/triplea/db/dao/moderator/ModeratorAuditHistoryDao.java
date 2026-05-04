package org.triplea.db.dao.moderator;

import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/**
 * Interface for adding new moderator audit records to database. These records keep track of which
 * actions moderators have taken, who the target was and which moderator took the action.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ModeratorAuditHistoryDao {
  private final Jdbi jdbi;

  /** The set of moderator actions. */
  public enum AuditAction {
    BAN_MAC,
    BAN_USERNAME,
    REMOVE_USERNAME_BAN,
    BOOT_GAME,
    BOOT_USER_FROM_BOT,
    BOOT_USER_FROM_LOBBY,
    BAN_PLAYER_FROM_BOT,
    ADD_BAD_WORD,
    REMOVE_BAD_WORD,
    BAN_USER,
    REMOVE_USER_BAN,
    ADD_MODERATOR,
    REMOVE_MODERATOR,
    ADD_SUPER_MOD,
    DISCONNECT_USER,
    DISCONNECT_GAME,
    REMOTE_SHUTDOWN,
  }

  /** Parameters needed when adding an audit record. */
  @Getter
  @Builder
  @ToString
  @EqualsAndHashCode
  public static final class AuditArgs {
    @Nonnull private final Integer moderatorUserId;
    @Nonnull private final AuditAction actionName;
    @Nonnull private final String actionTarget;
  }

  public void addAuditRecord(AuditArgs auditArgs) {
    final int rowsInserted =
        insertAuditRecord(
            auditArgs.moderatorUserId, auditArgs.actionName.toString(), auditArgs.actionTarget);
    Preconditions.checkState(rowsInserted == 1);
  }

  public int insertAuditRecord(int moderatorId, String actionName, String actionTarget) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into moderator_action_history
                      (lobby_user_id, action_name, action_target)
                    values (:moderatorId, :actionName, :actionTarget)
                    """)
                .bind("moderatorId", moderatorId)
                .bind("actionName", actionName)
                .bind("actionTarget", actionTarget)
                .execute());
  }

  public List<ModeratorAuditHistoryRecord> lookupHistoryItems(int rowOffset, int rowCount) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        h.date_created,
                        u.username,
                        h.action_name,
                        h.action_target
                      from moderator_action_history h
                      join lobby_user u on u.id = h.lobby_user_id
                      order by h.date_created desc
                      offset :rowOffset rows
                      fetch next :rowCount rows only
                    """)
                .bind("rowOffset", rowOffset)
                .bind("rowCount", rowCount)
                .map(ConstructorMapper.of(ModeratorAuditHistoryRecord.class))
                .list());
  }
}
