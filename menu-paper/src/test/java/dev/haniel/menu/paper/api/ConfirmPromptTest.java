package dev.haniel.menu.paper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.item.Icon;
import org.junit.jupiter.api.Test;

/**
 * Probes the pure value-object behaviour of {@link ConfirmPrompt} (the dialog flow is smoke-only).
 */
class ConfirmPromptTest {

  @Test
  void hasSensibleDefaults() {
    ConfirmPrompt prompt = ConfirmPrompt.create();

    assertEquals("<red>Are you sure?", prompt.title());
    assertEquals(11, prompt.confirmChoice().slot());
    assertEquals(15, prompt.cancelChoice().slot());
    assertEquals("LIME_WOOL", prompt.confirmChoice().icon().material());
    assertEquals("RED_WOOL", prompt.cancelChoice().icon().material());
  }

  @Test
  void titledSetsTheTitle() {
    assertEquals("<gold>Delete?", ConfirmPrompt.titled("<gold>Delete?").title());
  }

  @Test
  void overridesIconsKeepingTheirSlots() {
    ConfirmPrompt prompt =
        ConfirmPrompt.create()
            .confirm(Icon.of("EMERALD").named("<green>Buy"))
            .cancel(Icon.of("BARRIER").named("<red>No"));

    assertEquals("EMERALD", prompt.confirmChoice().icon().material());
    assertEquals(11, prompt.confirmChoice().slot(), "overriding the icon keeps the default slot");
    assertEquals("BARRIER", prompt.cancelChoice().icon().material());
    assertEquals(15, prompt.cancelChoice().slot());
  }

  @Test
  void runsOnlyTheChosenHandler() {
    boolean[] confirmed = {false};
    boolean[] cancelled = {false};
    ConfirmPrompt prompt =
        ConfirmPrompt.create()
            .onConfirm(() -> confirmed[0] = true)
            .onCancel(() -> cancelled[0] = true);

    prompt.runConfirm();

    assertTrue(confirmed[0]);
    assertFalse(cancelled[0]);
  }

  @Test
  void defaultHandlersAreNoOps() {
    ConfirmPrompt prompt = ConfirmPrompt.create();

    prompt.runConfirm();
    prompt.runCancel();
  }
}
