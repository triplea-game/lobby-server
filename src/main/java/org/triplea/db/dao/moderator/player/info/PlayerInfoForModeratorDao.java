package org.triplea.db.dao.moderator.player.info;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/**
 * DAO used to lookup player correlation information for moderators. Answers questions such as
 *
 * <ul>
 *   <li>"how many times and was this player banned?"
 *   <li>"which other names were used by this same IP and system-id? (presumably the same player)"
 * </ul>
 */
@ApplicationScoped
@RequiredArgsConstructor
public class PlayerInfoForModeratorDao {
  @Inject private final Jdbi jdbi;

  public List<PlayerAliasRecord> lookupPlayerAliasRecords(String systemId, String ip) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    "select distinct"
                        + "    username as name,"
                        + "    ip as ip,"
                        + "    system_id as systemId,"
                        + "    max(access_time) as accessTime"
                        + "  from access_log"
                        + "  where "
                        + "    access_time > (now() - '14 day'::interval)"
                        + "    and ("
                        + "      ip = :ip::inet"
                        + "      or system_id = :systemId"
                        + "    )"
                        + "  group by name, ip, systemId"
                        + "  order by accessTime desc")
                .bind("systemId", systemId)
                .bind("ip", ip)
                .map(ConstructorMapper.of(PlayerAliasRecord.class))
                .list());
  }

  List<PlayerBanRecord> lookupPlayerBanRecords(String systemId, String ip) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    "select"
                        + "    username,"
                        + "    ip,"
                        + "    system_id,"
                        + "    date_created,"
                        + "    ban_expiry"
                        + "  from banned_user"
                        + "  where "
                        + "    ip = :ip::inet"
                        + "    or system_id = :systemId"
                        + "  order by ban_expiry desc")
                .bind("systemId", systemId)
                .bind("ip", ip)
                .map(ConstructorMapper.of(PlayerBanRecord.class))
                .list());
  }
}
