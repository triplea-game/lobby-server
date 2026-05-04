package org.triplea.db.dao.user.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserRoleDao {
  private final Jdbi jdbi;

  public int lookupRoleId(String userRole) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select id from user_role where name = :userRole
                    """)
                .bind("userRole", userRole)
                .mapTo(Integer.class)
                .one());
  }
}
