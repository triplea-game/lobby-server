package org.triplea.db.dao.user.ban;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

/** DAO for managing user bans (CRUD operations). */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserBanDao {
  private final Jdbi jdbi;

  public List<UserBanRecord> lookupBans() {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        public_id,
                        username,
                        system_id,
                        ip,
                        ban_expiry,
                        date_created
                      from banned_user
                      where ban_expiry > now()
                      order by date_created desc
                    """)
                .map(ConstructorMapper.of(UserBanRecord.class))
                .list());
  }

  public Optional<BanLookupRecord> lookupBan(String ip, String systemId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        public_id,
                        ban_expiry
                      from banned_user
                      where (ip = :ip::inet or system_id = :systemId)
                        and ban_expiry > now()
                      order by ban_expiry desc
                      limit 1
                    """)
                .bind("ip", ip)
                .bind("systemId", systemId)
                .map(ConstructorMapper.of(BanLookupRecord.class))
                .findOne());
  }

  public boolean isBannedByIp(String ip) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select exists (
                      select *
                      from banned_user
                      where ip = :ip::inet and ban_expiry > now()
                    )
                    """)
                .bind("ip", ip)
                .mapTo(Boolean.class)
                .one());
  }

  public Optional<String> lookupUsernameByBanId(String banId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select username
                      from banned_user
                      where public_id = :banId
                    """)
                .bind("banId", banId)
                .mapTo(String.class)
                .findOne());
  }

  public int removeBan(String banId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    delete from banned_user where public_id = :banId
                    """)
                .bind("banId", banId)
                .execute());
  }

  public int addBan(String banId, String username, String systemId, String ip, long banMinutes) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into banned_user
                    (public_id, username, system_id, ip, ban_expiry) values
                    (:banId, :username, :systemId, :ip::inet, now() + :banMinutes * '1 minute'::interval)
                    """)
                .bind("banId", banId)
                .bind("username", username)
                .bind("systemId", systemId)
                .bind("ip", ip)
                .bind("banMinutes", banMinutes)
                .execute());
  }
}
