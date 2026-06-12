package dev.haniel.menu.paper.visibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.annotation.Visible;
import dev.haniel.menu.compiler.InvalidMenuException;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class VisibilityRulesTest {

  private static final Map<String, Integer> SLOTS = Map.of("admin", 3, "always", 5);

  static final class Sample {
    boolean adminVisible;

    @Visible("admin")
    private boolean canSeeAdmin(Player player) {
      return adminVisible;
    }

    @Visible("always")
    private boolean alwaysShown() {
      return true;
    }
  }

  @Test
  void hidesOnlyTheButtonsWhoseRuleReturnsFalse() {
    Sample instance = new Sample();
    instance.adminVisible = false;

    Set<Integer> hidden =
        VisibilityRules.of(Sample.class).hiddenSlots(instance, mock(Player.class), SLOTS);

    assertEquals(
        Set.of(3), hidden, "the admin button (slot 3) is hidden; the always button is not");
  }

  @Test
  void hidesNothingWhenEveryRulePasses() {
    Sample instance = new Sample();
    instance.adminVisible = true;

    Set<Integer> hidden =
        VisibilityRules.of(Sample.class).hiddenSlots(instance, mock(Player.class), SLOTS);

    assertTrue(hidden.isEmpty());
  }

  @Test
  void cachesRulesPerClass() {
    assertSame(VisibilityRules.of(Sample.class), VisibilityRules.of(Sample.class));
  }

  @Test
  void reportsWhetherTheClassHasRules() {
    assertFalse(VisibilityRules.of(Sample.class).isEmpty());
    assertTrue(VisibilityRules.of(NoRules.class).isEmpty());
  }

  static final class NoRules {}

  @Test
  void rejectsAVisibleIdThatMatchesNoButton() {
    Sample instance = new Sample();

    InvalidMenuException thrown =
        assertThrows(
            InvalidMenuException.class,
            () ->
                VisibilityRules.of(Sample.class)
                    .hiddenSlots(instance, mock(Player.class), Map.of("always", 5)));
    assertTrue(thrown.getMessage().contains("admin"));
  }

  static final class BadReturn {
    @Visible("x")
    private void notBoolean(Player player) {}
  }

  @Test
  void rejectsANonBooleanRule() {
    InvalidMenuException thrown =
        assertThrows(InvalidMenuException.class, () -> VisibilityRules.of(BadReturn.class));
    assertTrue(thrown.getMessage().contains("must return boolean"));
  }

  static final class BadParam {
    @Visible("x")
    private boolean wrongArg(String notAPlayer) {
      return true;
    }
  }

  @Test
  void rejectsARuleWithANonPlayerParameter() {
    assertThrows(InvalidMenuException.class, () -> VisibilityRules.of(BadParam.class));
  }

  static final class Duplicate {
    @Visible("dup")
    private boolean a() {
      return true;
    }

    @Visible("dup")
    private boolean b() {
      return true;
    }
  }

  @Test
  void rejectsTwoRulesForTheSameButton() {
    InvalidMenuException thrown =
        assertThrows(InvalidMenuException.class, () -> VisibilityRules.of(Duplicate.class));
    assertTrue(thrown.getMessage().contains("Duplicate"));
  }

  static final class Throwing {
    @Visible("boom")
    private boolean boom(Player player) {
      throw new IllegalStateException("nope");
    }
  }

  @Test
  void wrapsAThrowingRule() {
    Throwing instance = new Throwing();

    assertThrows(
        VisibilityException.class,
        () ->
            VisibilityRules.of(Throwing.class)
                .hiddenSlots(instance, mock(Player.class), Map.of("boom", 1)));
  }
}
