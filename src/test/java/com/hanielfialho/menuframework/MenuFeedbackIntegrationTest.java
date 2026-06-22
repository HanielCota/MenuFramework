package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.hanielfialho.menuframework.api.feedback.MenuFeedbackSignal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
final class MenuFeedbackIntegrationTest extends MenuManagerTestSupport {

  @Test
  void feedbackIsEmittedAfterSuccessfulSynchronousInteraction() {
    MenuFeedbackSignal signal = MenuFeedbackSignal.of("test.success");
    List<MenuFeedbackSignal> emitted = new CopyOnWriteArrayList<>();
    this.recreateFrameworkWithFeedback(emitted);
    RecordingMenu menu = new RecordingMenu("Feedback");
    menu.onPrimary(
        interaction -> {
          interaction.feedback(signal);
          interaction.updateState(MenuState::increment);
        });

    this.open(menu, MenuState.initial());
    this.dispatchPrimaryClick();

    assertEquals(List.of(signal), emitted);
  }

  @Test
  void feedbackIsDiscardedWhenHandlerFails() {
    MenuFeedbackSignal signal = MenuFeedbackSignal.of("test.discarded");
    List<MenuFeedbackSignal> emitted = new CopyOnWriteArrayList<>();
    this.recreateFrameworkWithFeedback(emitted);
    RecordingMenu menu = new RecordingMenu("Feedback failure");
    menu.onPrimary(
        interaction -> {
          interaction.feedback(signal);
          throw new IllegalStateException("boom");
        });

    this.open(menu, MenuState.initial());
    this.dispatchPrimaryClick();

    assertEquals(List.of(), emitted);
  }

  @Test
  void terminalFeedbackIsEmittedOnlyAfterCloseCompletes() {
    MenuFeedbackSignal signal = MenuFeedbackSignal.of("test.close");
    List<MenuFeedbackSignal> emitted = new CopyOnWriteArrayList<>();
    this.recreateFrameworkWithFeedback(emitted);
    RecordingMenu menu = new RecordingMenu("Terminal feedback");
    menu.onPrimary(
        interaction -> {
          interaction.feedback(signal);
          interaction.close();
        });

    this.open(menu, MenuState.initial());
    this.dispatchPrimaryClick();

    assertEquals(List.of(), emitted);

    this.advanceTicks(2L);

    assertFalse(this.menus.isOpen(this.player));
    assertEquals(List.of(signal), emitted);
  }

  private void recreateFrameworkWithFeedback(List<MenuFeedbackSignal> emitted) {
    this.framework.shutdown();
    MenuFrameworkConfiguration configuration =
        MenuFrameworkConfiguration.builder()
            .defaultFeedback((context, signal) -> emitted.add(signal))
            .build();
    this.framework = MenuFramework.create(this.plugin, configuration);
    this.menus = this.framework.menus();
  }
}
