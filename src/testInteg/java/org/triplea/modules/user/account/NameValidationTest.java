package org.triplea.modules.user.account;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.db.dao.moderator.BadWordsDao;
import org.triplea.db.dao.user.UserJdbiDao;
import org.triplea.db.dao.username.ban.UsernameBanDao;

@ExtendWith(MockitoExtension.class)
class NameValidationTest {

  private static final String NAME = "example-name";
  private static final String ERROR_MESSAGE = "error-sample";

  @Mock private Function<String, Optional<String>> syntaxValidation;
  @Mock private BadWordsDao badWordsDao;
  @Mock private UserJdbiDao userJdbiDao;
  @Mock private UsernameBanDao usernameBanDao;

  private NameValidation nameValidation;

  @BeforeEach
  void setUp() {
    nameValidation =
        NameValidation.builder()
            .syntaxValidation(syntaxValidation)
            .badWordsDao(badWordsDao)
            .userJdbiDao(userJdbiDao)
            .usernameBanDao(usernameBanDao)
            .build();
  }

  @Test
  void invalidSyntax() {
    when(syntaxValidation.apply(NAME)).thenReturn(Optional.of(ERROR_MESSAGE));

    final Optional<String> result = nameValidation.apply(NAME);

    assertThat(result, isPresentAndIs(ERROR_MESSAGE));
  }

  @Test
  void containsBadWord() {
    when(syntaxValidation.apply(NAME)).thenReturn(Optional.empty());
    when(badWordsDao.containsBadWord(NAME)).thenReturn(true);

    final Optional<String> result = nameValidation.apply(NAME);

    assertThat(result, isPresent());
  }

  @Test
  void valid() {
    when(syntaxValidation.apply(NAME)).thenReturn(Optional.empty());
    when(badWordsDao.containsBadWord(NAME)).thenReturn(false);

    final Optional<String> result = nameValidation.apply(NAME);

    assertThat(result, isEmpty());
  }
}
