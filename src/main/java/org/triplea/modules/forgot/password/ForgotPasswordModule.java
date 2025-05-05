package org.triplea.modules.forgot.password;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.temp.password.TempPasswordHistoryDao;
import org.triplea.http.client.forgot.password.ForgotPasswordRequest;
import org.triplea.java.StringUtils;

/**
 * Module to orchestrate generating a temporary password for a user, storing that password in the
 * temp password table and sending that password in an email to the user.
 */
@Builder
public class ForgotPasswordModule implements BiFunction<String, ForgotPasswordRequest, String> {

  @VisibleForTesting
  static final String ERROR_TOO_MANY_REQUESTS = "Error, too many password reset attempts";

  @VisibleForTesting
  static final String ERROR_BAD_USER_OR_EMAIL =
      "Error, temp password not generated - check username and email";

  @VisibleForTesting
  static final String SUCCESS_MESSAGE = "A temporary password has been sent to your email.";

  @Nonnull private final BiConsumer<String, String> passwordEmailSender;
  @Nonnull private final PasswordGenerator passwordGenerator;
  @Nonnull private final TempPasswordPersistence tempPasswordPersistence;
  @Nonnull private final TempPasswordHistory tempPasswordHistory;

  public static BiFunction<String, ForgotPasswordRequest, String> build(
      final boolean sendEmailsEnabled, final Jdbi jdbi, final String smtpHost, final int smtpPort) {
    final Properties props = new Properties();
    props.put("mail.smtp.host", smtpHost);
    props.put("mail.smtp.port", smtpPort);

    return ForgotPasswordModule.builder()
        .passwordEmailSender(
            PasswordEmailSender.builder()
                .sendEmailsEnabled(sendEmailsEnabled)
                .smtpProperties(props)
                .build())
        .passwordGenerator(new PasswordGenerator())
        .tempPasswordPersistence(TempPasswordPersistence.newInstance(jdbi))
        .tempPasswordHistory(new TempPasswordHistory(jdbi.onDemand(TempPasswordHistoryDao.class)))
        .build();
  }

  @Override
  public String apply(final String inetAddress, final ForgotPasswordRequest forgotPasswordRequest) {
    checkArgument(!StringUtils.isNullOrBlank(inetAddress));
    try {
      //noinspection ResultOfMethodCallIgnored
      InetAddress.getAllByName(inetAddress);
    } catch (final UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IP address: " + inetAddress);
    }
    checkNotNull(forgotPasswordRequest);
    checkArgument(!StringUtils.isNullOrBlank(forgotPasswordRequest.getUsername()));
    checkArgument(!StringUtils.isNullOrBlank(forgotPasswordRequest.getEmail()));

    if (!tempPasswordHistory.allowRequestFromAddress(inetAddress)) {
      return ERROR_TOO_MANY_REQUESTS;
    }
    tempPasswordHistory.recordTempPasswordRequest(inetAddress, forgotPasswordRequest.getUsername());

    final String generatedPassword = passwordGenerator.generatePassword();
    if (!tempPasswordPersistence.storeTempPassword(forgotPasswordRequest, generatedPassword)) {
      return ERROR_BAD_USER_OR_EMAIL;
    }
    passwordEmailSender.accept(forgotPasswordRequest.getEmail(), generatedPassword);

    return SUCCESS_MESSAGE;
  }
}
