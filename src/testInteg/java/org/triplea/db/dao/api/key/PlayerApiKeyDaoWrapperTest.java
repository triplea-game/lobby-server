package org.triplea.db.dao.api.key;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.db.dao.user.UserJdbiDao;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.db.dao.user.role.UserRoleDao;
import org.triplea.db.dao.user.role.UserRoleLookup;
import org.triplea.domain.data.ApiKey;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.domain.data.SystemId;
import org.triplea.domain.data.UserName;

@ExtendWith(MockitoExtension.class)
class PlayerApiKeyDaoWrapperTest {

  private static final ApiKey API_KEY = ApiKey.of("api-key");
  private static final String HASHED_KEY = "Dead, rainy shores proud swashbuckler";
  private static final UserName PLAYER_NAME = UserName.of("The_captain");
  private static final PlayerChatId PLAYER_CHAT_ID = PlayerChatId.of("player-chat-id");
  private static final SystemId SYSTEM_ID = SystemId.of("system-id");
  private static final int ANONYMOUS_ROLE_ID = 123;
  private static final PlayerIdentifiersByApiKeyLookup PLAYER_ID_LOOKUP =
      PlayerIdentifiersByApiKeyLookup.builder()
          .userName(PLAYER_NAME.getValue())
          .systemId(SYSTEM_ID.getValue())
          .ip("ip")
          .build();

  private static final UserRoleLookup USER_ROLE_LOOKUP =
      UserRoleLookup.builder().userId(10).userRoleId(20).build();

  private static final InetAddress IP;

  static {
    try {
      IP = InetAddress.getLocalHost();
    } catch (final UnknownHostException e) {
      throw new IllegalStateException(e);
    }
  }

  @Mock private PlayerApiKeyDao lobbyApiKeyDao;
  @Mock private UserJdbiDao userJdbiDao;
  @Mock private UserRoleDao userRoleDao;
  @Mock private Supplier<ApiKey> keyMaker;
  @Mock private Function<ApiKey, String> keyHashingFunction;

  @InjectMocks private PlayerApiKeyDaoWrapper wrapper;

  @Mock private PlayerApiKeyLookupRecord apiKeyUserData;

  @Nested
  class LookupByApiKey {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void foundCase() {
      givenKeyLookupResult(Optional.of(apiKeyUserData));

      final Optional<PlayerApiKeyLookupRecord> result = wrapper.lookupByApiKey(API_KEY);

      assertThat(result, isPresent());
      assertThat(result.get(), sameInstance(apiKeyUserData));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void givenKeyLookupResult(final Optional<PlayerApiKeyLookupRecord> dataResult) {
      when(keyHashingFunction.apply(API_KEY)).thenReturn(HASHED_KEY);
      when(lobbyApiKeyDao.lookupByApiKey(HASHED_KEY)).thenReturn(dataResult);
    }

    @Test
    void notFoundCase() {
      givenKeyLookupResult(Optional.empty());

      final Optional<PlayerApiKeyLookupRecord> result = wrapper.lookupByApiKey(API_KEY);

      assertThat(result, isEmpty());
    }
  }

  @Nested
  class NewKey {
    @Test
    void anonymousUserNewKey() {
      when(keyMaker.get()).thenReturn(API_KEY);
      when(keyHashingFunction.apply(API_KEY)).thenReturn(HASHED_KEY);
      when(userJdbiDao.lookupUserIdAndRoleIdByUserName(PLAYER_NAME.getValue()))
          .thenReturn(Optional.empty());
      when(userRoleDao.lookupRoleId(UserRole.ANONYMOUS)).thenReturn(ANONYMOUS_ROLE_ID);
      when(lobbyApiKeyDao.storeKey(
              PLAYER_NAME.getValue(),
              null,
              ANONYMOUS_ROLE_ID,
              PLAYER_CHAT_ID.getValue(),
              HASHED_KEY,
              SYSTEM_ID.getValue(),
              IP.getHostAddress()))
          .thenReturn(1);

      final ApiKey result = wrapper.newKey(PLAYER_NAME, IP, SYSTEM_ID, PLAYER_CHAT_ID);

      assertThat(result, is(API_KEY));
    }

    @Test
    void registeredUserKey() {
      when(keyMaker.get()).thenReturn(API_KEY);
      when(keyHashingFunction.apply(API_KEY)).thenReturn(HASHED_KEY);
      when(userJdbiDao.lookupUserIdAndRoleIdByUserName(PLAYER_NAME.getValue()))
          .thenReturn(Optional.of(USER_ROLE_LOOKUP));
      when(lobbyApiKeyDao.storeKey(
              PLAYER_NAME.getValue(),
              USER_ROLE_LOOKUP.getUserId(),
              USER_ROLE_LOOKUP.getUserRoleId(),
              PLAYER_CHAT_ID.getValue(),
              HASHED_KEY,
              SYSTEM_ID.getValue(),
              IP.getHostAddress()))
          .thenReturn(1);

      final ApiKey result = wrapper.newKey(PLAYER_NAME, IP, SYSTEM_ID, PLAYER_CHAT_ID);

      assertThat(result, is(API_KEY));
    }
  }

  @Test
  void lookupByPlayerChatId() {
    when(lobbyApiKeyDao.lookupByPlayerChatId(PLAYER_CHAT_ID.getValue()))
        .thenReturn(Optional.of(PLAYER_ID_LOOKUP));

    final Optional<PlayerIdentifiersByApiKeyLookup> result =
        wrapper.lookupPlayerByChatId(PLAYER_CHAT_ID);

    assertThat(result, isPresentAndIs(PLAYER_ID_LOOKUP));
  }

  @Test
  void lookupUserByPlayerChatId() {
    when(lobbyApiKeyDao.lookupPlayerIdByPlayerChatId(PLAYER_CHAT_ID.getValue()))
        .thenReturn(Optional.of(123));

    final Optional<Integer> result = wrapper.lookupUserIdByChatId(PLAYER_CHAT_ID);

    assertThat(result, isPresentAndIs(123));
  }

  @Test
  void lookupUserByPlayerChatIdNotFoundCase() {
    when(lobbyApiKeyDao.lookupPlayerIdByPlayerChatId(PLAYER_CHAT_ID.getValue()))
        .thenReturn(Optional.empty());

    final Optional<Integer> result = wrapper.lookupUserIdByChatId(PLAYER_CHAT_ID);

    assertThat(result, isEmpty());
  }
}
