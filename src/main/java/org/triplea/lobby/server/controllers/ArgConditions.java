package org.triplea.lobby.server.controllers;

import jakarta.ws.rs.BadRequestException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ArgConditions {
  public static <T> void assertNotNull(T object, String message) {
    if (object == null) {
      throw new BadRequestException(message);
    }
  }

  public static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new BadRequestException(message);
    }
  }
}
