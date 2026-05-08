package org.triplea.lobby.server.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class BadWordsControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String GET_PATH = "/lobby/moderator-toolbox/bad-words/get";
  private static final String ADD_PATH = "/lobby/moderator-toolbox/bad-words/add";
  private static final String REMOVE_PATH = "/lobby/moderator-toolbox/bad-words/remove";

  private LobbyHttpClientHelper client;

  @BeforeEach
  void setUp() {
    client = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  private List<String> getBadWords() {
    return Arrays.asList(client.get(GET_PATH, String[].class));
  }

  @Test
  void badRequests() {
    assertBadRequest(() -> client.post(ADD_PATH, ""));
    assertBadRequest(() -> client.post(REMOVE_PATH, ""));
  }

  @Test
  void listBadWords() {
    assertThat(getBadWords()).isNotEmpty();
  }

  @Test
  void removeBadWord() {
    final List<String> badWords = getBadWords();
    final String firstBadWord = badWords.get(0);

    client.post(REMOVE_PATH, firstBadWord);

    assertThat(getBadWords()).doesNotContain(firstBadWord);
  }

  @Test
  void addBadWord() {
    assertThat(getBadWords()).doesNotContain("bad-word-to-be-added");

    client.post(ADD_PATH, "bad-word-to-be-added");

    assertThat(getBadWords()).contains("bad-word-to-be-added");
  }
}
