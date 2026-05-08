package org.triplea.modules.forgot.password;

import com.google.common.annotations.VisibleForTesting;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.temp.password.TempPasswordDao;
import org.triplea.http.client.forgot.password.ForgotPasswordRequest;
import org.triplea.modules.user.account.PasswordBCrypter;

/**
 * Stores a user temporary password in database. When we generate a new temporary password, all
 * existing temporary passwords are invalidated so that a user only has one temp password at a time.
 */
@AllArgsConstructor(
    access = AccessLevel.PACKAGE,
    onConstructor_ = {@VisibleForTesting})
class TempPasswordPersistence {
  @Nonnull private final TempPasswordDao tempPasswordDao;
  @Nonnull private final Function<String, String> passwordHasher;
  @Nonnull private final Function<String, String> hashedPasswordBcrypter;

  static TempPasswordPersistence newInstance(final Jdbi jdbi) {
    return new TempPasswordPersistence(
        new TempPasswordDao(jdbi),
        TempPasswordPersistence::hashPasswordWithSalt,
        PasswordBCrypter::hashPassword);
  }

  private static String hashPasswordWithSalt(final String password) {
    if (password.isBlank()) {
      return password;
    }
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-512")
                  .digest(("TripleA" + password).getBytes(StandardCharsets.UTF_8)))
          .toLowerCase(Locale.ROOT);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-512 is not supported!", e);
    }
  }

  boolean storeTempPassword(
      final ForgotPasswordRequest forgotPasswordRequest, final String generatedPassword) {
    final String hashedPass = passwordHasher.apply(generatedPassword);
    final String tempPass = hashedPasswordBcrypter.apply(hashedPass);
    return tempPasswordDao.insertTempPassword(
        forgotPasswordRequest.getUsername(), forgotPasswordRequest.getEmail(), tempPass);
  }
}
