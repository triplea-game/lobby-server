package org.triplea.db.dao.temp.password;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

/**
 * DAO for CRUD operations on temp password history table. A table that stores a history of requests
 * for temporary passwords.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TempPasswordHistoryDao {
  private final Jdbi jdbi;

  /**
   * Returns the number of temp password requests made in the last day from a particular IP address.
   */
  public int countRequestsFromAddress(String address) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                    select count(*)
                     from temp_password_request_history
                     where inetaddress = :inetAddress::inet
                       and date_created >  (now() - '1 day'::interval)
                    """)
                .bind("inetAddress", address)
                .mapTo(Integer.class)
                .one());
  }

  /**
   * Records a temp password request being made from a given IP address and for a given username.
   */
  public void recordTempPasswordRequest(String address, String username) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into temp_password_request_history(inetaddress,  username)
                     values(:inetaddress::inet, :username)
                    """)
                .bind("inetaddress", address)
                .bind("username", username)
                .execute());
  }
}
