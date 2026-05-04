package org.triplea.db.dao.access.log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/**
 * Provides access to the access log table. This is a table that records user data as they enter the
 * lobby. Useful for statistics and for banning.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccessLogDao {

  private final Jdbi jdbi;

  public List<AccessLogRecord> fetchAccessLogRows(
      int rowOffset, int rowCount, String username, String ip, String systemId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    "select"
                        + "    access_time,"
                        + "    username,"
                        + "    ip,"
                        + "    system_id,"
                        + "    (lobby_user_id is not null) as registered"
                        + "  from access_log"
                        + "  where username like :username"
                        + "     and host(ip) like :ip"
                        + "     and system_id like :systemId"
                        + "  order by access_time desc"
                        + "  offset :rowOffset rows"
                        + "  fetch next :rowCount rows only")
                .bind("rowOffset", rowOffset)
                .bind("rowCount", rowCount)
                .bind("username", "%" + username + "%")
                .bind("ip", "%" + ip + "%")
                .bind("systemId", "%" + systemId + "%")
                .map(ConstructorMapper.of(AccessLogRecord.class))
                .list());
  }

  public int insertUserAccessRecord(String username, String ip, String systemId) {

    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    "insert into access_log(username, ip, system_id, lobby_user_id)\n"
                        + "values ("
                        + "  :username,"
                        + "  :ip::inet,"
                        + "  :systemId,"
                        + "  (select id from lobby_user where username = :username))")
                .bind("username", username)
                .bind("ip", ip)
                .bind("systemId", systemId)
                .execute());
  }
}
