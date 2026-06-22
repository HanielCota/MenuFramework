package com.hanielfialho.menuframework.api.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class MenuTaskContractsTest {

  @Test
  void taskKeyAcceptsStableNamespacedStyleValues() {
    MenuTaskKey key = MenuTaskKey.of("shop.products-refresh_1");

    assertEquals("shop.products-refresh_1", key.value());
  }

  @Test
  void taskKeyRejectsUppercaseBlankAndOversizedValues() {
    assertThrows(IllegalArgumentException.class, () -> MenuTaskKey.of("Products"));

    assertThrows(IllegalArgumentException.class, () -> MenuTaskKey.of(""));

    assertThrows(IllegalArgumentException.class, () -> MenuTaskKey.of("a".repeat(65)));
  }

  @Test
  void scheduleFactoriesConfigureInitialDelayAndPeriod() {
    MenuTaskSchedule delayed = MenuTaskSchedule.everyTicks(20L);
    MenuTaskSchedule nextTick = MenuTaskSchedule.startingNextTick(4L);

    assertEquals(20L, delayed.initialDelayTicks());
    assertEquals(20L, delayed.periodTicks());
    assertEquals(1L, nextTick.initialDelayTicks());
    assertEquals(4L, nextTick.periodTicks());
  }

  @Test
  void scheduleRejectsNonPositiveTicks() {
    assertThrows(IllegalArgumentException.class, () -> MenuTaskSchedule.of(0L, 1L));

    assertThrows(IllegalArgumentException.class, () -> MenuTaskSchedule.of(1L, 0L));
  }

  @Test
  void continueResultPreservesStateWithoutRenderingOrStopping() {
    Object state = new Object();
    MenuTickResult<Object> result = MenuTickResult.continueTask();

    assertEquals(state, result.resolveState(state));
    assertFalse(result.renderRequested());
    assertFalse(result.stopRequested());
  }

  @Test
  void updateResultReplacesStateAndRequestsRender() {
    MenuTickResult<String> result = MenuTickResult.update("next");

    assertEquals("next", result.resolveState("current"));
    assertTrue(result.renderRequested());
    assertFalse(result.stopRequested());
  }

  @Test
  void stopWithStateCombinesReplacementRenderAndStop() {
    MenuTickResult<Integer> result = MenuTickResult.stopWithState(2);

    assertEquals(2, result.resolveState(1));
    assertTrue(result.renderRequested());
    assertTrue(result.stopRequested());
  }

  @Test
  void stateReplacementCannotBeNull() {
    assertThrows(NullPointerException.class, () -> MenuTickResult.update(null));

    assertThrows(NullPointerException.class, () -> MenuTickResult.stopWithState(null));
  }
}
