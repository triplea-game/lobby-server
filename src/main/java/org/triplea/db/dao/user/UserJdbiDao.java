package org.triplea.db.dao.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.db.dao.user.role.UserRoleLookup;

/** Data access object for the users table. */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserJdbiDao {
  private final Jdbi jdbi;

  public Optional<Integer> lookupUserIdByName(String username) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select id from lobby_user where username = :username
                    """)
                .bind("username", username)
                .mapTo(Integer.class)
                .findOne());
  }

  public Optional<String> getPassword(String username) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select bcrypt_password from lobby_user where username = :username
                    """)
                .bind("username", username)
                .mapTo(String.class)
                .findOne());
  }

  public int updatePassword(int userId, String newPassword) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    update lobby_user set bcrypt_password = :newPassword where id = :userId
                    """)
                .bind("userId", userId)
                .bind("newPassword", newPassword)
                .execute());
  }

  public String fetchEmail(int userId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select email from lobby_user where id = :userId
                    """)
                .bind("userId", userId)
                .mapTo(String.class)
                .one());
  }

  public int updateEmail(int userId, String newEmail) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    update lobby_user set email = :newEmail where id = :userId
                    """)
                .bind("userId", userId)
                .bind("newEmail", newEmail)
                .execute());
  }

  public Optional<UserRoleLookup> lookupUserIdAndRoleIdByUserName(String username) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select
                        id,
                        user_role_id
                      from lobby_user
                      where username = :username
                    """)
                .bind("username", username)
                .map(ConstructorMapper.of(UserRoleLookup.class))
                .findOne());
  }

  public Optional<String> lookupUserRoleByUserName(String username) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select ur.name from user_role ur
                    join lobby_user lu on lu.user_role_id = ur.id
                    where lu.username = :username
                    """)
                .bind("username", username)
                .mapTo(String.class)
                .findOne());
  }

  public int createUser(String username, String email, String password) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into lobby_user(username, email, bcrypt_password, user_role_id)
                    select
                    :username, :email, :password, (select id from user_role where name = '"""
                        + UserRole.PLAYER
                        + """
                    ') as role_id
                    """)
                .bind("username", username)
                .bind("email", email)
                .bind("password", password)
                .execute());
  }
}
