package dev.haniel.menu.paper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * The parse/confirm/cancel contract of {@link AnvilPrompt}, the part of anvil input that runs
 * without a server. The actual anvil event flow is covered by the in-game smoke test.
 */
class AnvilPromptTest {

  @Test
  void numericResolvesAValidInteger() {
    AtomicReference<Integer> confirmed = new AtomicReference<>();
    AnvilPrompt<Integer> prompt = AnvilPrompt.numeric().onConfirm(confirmed::set);

    Optional<Runnable> action = prompt.resolve("42");

    assertTrue(action.isPresent(), "a valid integer must resolve to a confirm action");
    action.get().run();
    assertEquals(42, confirmed.get());
  }

  @Test
  void numericTrimsSurroundingWhitespace() {
    AtomicReference<Integer> confirmed = new AtomicReference<>();

    AnvilPrompt.numeric().onConfirm(confirmed::set).resolve("  7 ").orElseThrow().run();

    assertEquals(7, confirmed.get());
  }

  @Test
  void numericRejectsNonNumericInputSoItReprompts() {
    AnvilPrompt<Integer> prompt = AnvilPrompt.numeric();

    assertFalse(prompt.resolve("abc").isPresent(), "non-numeric input must re-prompt");
    assertFalse(prompt.resolve("").isPresent(), "blank input must re-prompt");
  }

  @Test
  void textResolvesAnyValueIncludingTheRawString() {
    AtomicReference<String> confirmed = new AtomicReference<>();

    AnvilPrompt.text().onConfirm(confirmed::set).resolve("a name").orElseThrow().run();

    assertEquals("a name", confirmed.get());
  }

  @Test
  void cancelRunsTheCancelHandler() {
    AtomicBoolean cancelled = new AtomicBoolean();

    AnvilPrompt.text().onCancel(() -> cancelled.set(true)).cancel();

    assertTrue(cancelled.get());
  }

  @Test
  void exposesTitleAndInitialText() {
    AnvilPrompt<String> prompt = AnvilPrompt.text().title("<gray>Enter</gray>").initialText("seed");

    assertEquals("<gray>Enter</gray>", prompt.title());
    assertEquals("seed", prompt.initialText());
  }
}
