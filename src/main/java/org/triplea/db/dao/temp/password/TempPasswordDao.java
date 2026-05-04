package org.triplea.db.dao.temp.password;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/**
 * DAO for CRUD operations on temp password table. A table that stores temporary passwords issued to
 * them with the 'forgot password' feature.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TempPasswordDao {
  public static final String TEMP_PASSWORD_EXPIRATION = "1 day";

  private final Jdbi jdbi;

  public Optional<String> fetchTempPassword(String username) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select temp_password
                     from temp_password_request t
                     join lobby_user lu on lu.id = t.lobby_user_id
                     where lu.username = :username
                       and t.date_created >  (now() - '"""
                        + TEMP_PASSWORD_EXPIRATION
                        + """
                    '::interval)
                       and t.date_invalidated is null
                    """)
                .bind("username", username)
                .mapTo(String.class)
                .findOne());
  }

  public Optional<Integer> lookupUserIdByUsername(String username) {
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

  public void insertPassword(int userId, String password) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into temp_password_request
                     (lobby_user_id, temp_password)
                     values (:userId, :password)
                    """)
                .bind("userId", userId)
                .bind("password", password)
                .execute());
  }

  public Optional<Integer> lookupUserIdByUsernameAndEmail(String username, String email) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select id from lobby_user where username = :username and email = :email
                    """)
                .bind("username", username)
                .bind("email", email)
                .mapTo(Integer.class)
                .findOne());
  }

  public int invalidateTempPasswords(String playerName) {
    return jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    update temp_password_request
                     set date_invalidated = now()
                     where lobby_user_id = (select id from lobby_user where username = :playerName)
                       and date_invalidated is null
                    """)
                .bind("playerName", playerName)
                .execute());
  }

  public boolean insertTempPassword(
      final String username, final String email, final String password) {
    return lookupUserIdByUsernameAndEmail(username, email)
        .map(
            userId -> {
              invalidateTempPasswords(username);
              insertPassword(userId, password);
              return true;
            })
        .orElse(false);
  }
}
