package org.triplea.modules.user.account.login;

import com.google.common.base.Preconditions;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.access.log.AccessLogDao;

@Builder
class AccessLogUpdater implements Consumer<LoginRecord> {

  @Nonnull private final AccessLogDao accessLogDao;

  public static AccessLogUpdater build(final Jdbi jdbi) {
    return AccessLogUpdater.builder() //
        .accessLogDao(new AccessLogDao(jdbi))
        .build();
  }

  @Override
  public void accept(final LoginRecord loginRecord) {
    final int updateCount =
        accessLogDao.insertUserAccessRecord(
            loginRecord.getUserName().getValue(),
            loginRecord.getIp(),
            loginRecord.getSystemId().getValue());

    Preconditions.checkState(updateCount == 1);
  }
}
