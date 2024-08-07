package org.triplea.db;

import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.triplea.db.dao.access.log.AccessLogRecord;
import org.triplea.db.dao.api.key.PlayerApiKeyLookupRecord;
import org.triplea.db.dao.api.key.PlayerIdentifiersByApiKeyLookup;
import org.triplea.db.dao.moderator.ModeratorAuditHistoryRecord;
import org.triplea.db.dao.moderator.ModeratorUserDaoData;
import org.triplea.db.dao.moderator.player.info.PlayerAliasRecord;
import org.triplea.db.dao.moderator.player.info.PlayerBanRecord;
import org.triplea.db.dao.user.ban.BanLookupRecord;
import org.triplea.db.dao.user.ban.UserBanRecord;
import org.triplea.db.dao.user.history.PlayerHistoryRecord;
import org.triplea.db.dao.user.role.UserRoleLookup;
import org.triplea.db.dao.username.ban.UsernameBanRecord;

/** Utility to get connections to the Postgres lobby database. */
@Slf4j
@UtilityClass
public final class LobbyModuleRowMappers {
  /**
   * Returns all row mappers. These are classes that map result set values to corresponding return
   * objects.
   */
  public static List<RowMapperFactory> rowMappers() {
    return List.of(
        ConstructorMapper.factory(AccessLogRecord.class),
        ConstructorMapper.factory(BanLookupRecord.class),
        ConstructorMapper.factory(ModeratorAuditHistoryRecord.class),
        ConstructorMapper.factory(ModeratorUserDaoData.class),
        ConstructorMapper.factory(PlayerAliasRecord.class),
        ConstructorMapper.factory(PlayerApiKeyLookupRecord.class),
        ConstructorMapper.factory(PlayerBanRecord.class),
        ConstructorMapper.factory(PlayerHistoryRecord.class),
        ConstructorMapper.factory(PlayerIdentifiersByApiKeyLookup.class),
        ConstructorMapper.factory(UserBanRecord.class),
        ConstructorMapper.factory(UsernameBanRecord.class),
        ConstructorMapper.factory(UserRoleLookup.class));
  }
}
