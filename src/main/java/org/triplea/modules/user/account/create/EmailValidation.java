package org.triplea.modules.user.account.create;

import com.google.common.base.Strings;
import java.util.Optional;
import java.util.function.Function;
import org.triplea.http.client.lobby.LobbyConstants;

public class EmailValidation implements Function<String, Optional<String>> {
  private static final String QUOTED_STRING_REGEX = "\"(?:[^\"\\\\]|\\\\\\p{ASCII})*\"";
  private static final String ATOM_REGEX = "[^()<>@,;:\\\\\".\\[\\] \\x28\\p{Cntrl}]+";

  private static final String WORD_REGEX = "(?:" + ATOM_REGEX + "|" + QUOTED_STRING_REGEX + ")";

  private static final String SUBDOMAIN_REGEX =
      "(?:" + ATOM_REGEX + "|\\[(?:[^\\[\\]\\\\]|\\\\\\p{ASCII})*\\])";

  private static final String DOMAIN_REGEX = SUBDOMAIN_REGEX + "(?:\\." + SUBDOMAIN_REGEX + ")*";

  private static final String LOCAL_PART_REGEX = WORD_REGEX + "(?:\\." + WORD_REGEX + ")*";
  private static final String EMAIL_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

  @Override
  public Optional<String> apply(final String email) {
    return !Strings.isNullOrEmpty(email) && isValid(email)
        ? Optional.empty()
        : Optional.of("Invalid email address");
  }

  private static boolean isValid(final String emailAddress) {
    if (emailAddress == null) {
      return false;
    }
    if (emailAddress.length() > LobbyConstants.EMAIL_MAX_LENGTH) {
      return false;
    }
    if (!emailAddress.matches(EMAIL_REGEX)) {
      return false;
    }
    return true;
  }
}
