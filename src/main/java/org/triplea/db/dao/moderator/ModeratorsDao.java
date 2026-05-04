package org.triplea.db.dao.moderator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.triplea.db.dao.user.role.UserRole;

/** DAO for managing moderator users. */
@ApplicationScoped
@RequiredArgsConstructor
public class ModeratorsDao {
  @Inject private final Jdbi jdbi;

  public List<ModeratorUserDaoData> getUserByRole(Collection<String> roles) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        lu.username,
                        max(al.access_time) access_time
                      from lobby_user lu
                      left join access_log al on al.lobby_user_id = lu.id
                      join user_role ur on ur.id = lu.user_role_id
                      where ur.name in (<roles>)
                      group by lu.username
                      order by lu.username
                    """)
                .bindList("roles", roles)
                .map(ConstructorMapper.of(ModeratorUserDaoData.class))
                .list());
  }

  public List<ModeratorUserDaoData> getModerators() {
    return getUserByRole(Set.of(UserRole.MODERATOR, UserRole.ADMIN));
  }

  public int setRole(int userId, String role) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    update lobby_user
                      set user_role_id = (select id from user_role where name = :role)
                      where id = :userId
                    """)
                .bind("userId", userId)
                .bind("role", role)
                .execute());
  }
}
