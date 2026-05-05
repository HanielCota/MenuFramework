package com.github.hanielcota.menuframework.internal.session;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PlayerMenuHistory Tests")
class PlayerMenuHistoryTest {

  private PlayerMenuHistory history;
  private UUID playerUuid;

  @BeforeEach
  void setUp() {
    history = new PlayerMenuHistory();
    playerUuid = UUID.randomUUID();
  }

  @Test
  @DisplayName("Should push menu to history")
  void shouldPushMenu() {
    history.push(playerUuid, "menu1");
    assertTrue(history.hasHistory(playerUuid));
    assertEquals("menu1", history.peek(playerUuid).orElseThrow());
  }

  @Test
  @DisplayName("Should pop menu from history")
  void shouldPopMenu() {
    history.push(playerUuid, "menu1");
    history.push(playerUuid, "menu2");

    assertEquals("menu2", history.pop(playerUuid).orElseThrow());
    assertEquals("menu1", history.pop(playerUuid).orElseThrow());
    assertTrue(history.pop(playerUuid).isEmpty());
  }

  @Test
  @DisplayName("Should not push duplicate consecutive menus")
  void shouldNotPushDuplicateConsecutive() {
    history.push(playerUuid, "menu1");
    history.push(playerUuid, "menu1");

    assertEquals(1, history.getHistory(playerUuid).size());
  }

  @Test
  @DisplayName("Should limit history size")
  void shouldLimitHistorySize() {
    for (int i = 0; i < 15; i++) {
      history.push(playerUuid, "menu" + i);
    }

    assertEquals(10, history.getHistory(playerUuid).size());
  }

  @Test
  @DisplayName("Should clear history")
  void shouldClearHistory() {
    history.push(playerUuid, "menu1");
    history.clear(playerUuid);

    assertFalse(history.hasHistory(playerUuid));
  }

  @Test
  @DisplayName("Should peek without removing")
  void shouldPeekWithoutRemoving() {
    history.push(playerUuid, "menu1");

    assertEquals("menu1", history.peek(playerUuid).orElseThrow());
    assertEquals("menu1", history.peek(playerUuid).orElseThrow());
  }

  @Test
  @DisplayName("Should return empty for unknown player")
  void shouldReturnEmptyForUnknownPlayer() {
    assertFalse(history.hasHistory(playerUuid));
    assertTrue(history.pop(playerUuid).isEmpty());
    assertTrue(history.peek(playerUuid).isEmpty());
  }

  @Test
  @DisplayName("Should get history copy")
  void shouldGetHistoryCopy() {
    history.push(playerUuid, "menu1");
    history.push(playerUuid, "menu2");

    var copy = history.getHistory(playerUuid);
    assertEquals(2, copy.size());

    // Modifying copy should not affect original
    copy.clear();
    assertTrue(history.hasHistory(playerUuid));
  }
}
