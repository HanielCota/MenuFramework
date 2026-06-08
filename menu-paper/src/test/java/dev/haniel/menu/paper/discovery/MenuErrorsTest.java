package dev.haniel.menu.paper.discovery;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MenuErrorsTest {

  @Test
  void failIfAnyIsSilentWhenNoFailureRecorded() {
    assertDoesNotThrow(() -> new MenuErrors().failIfAny());
  }

  @Test
  void failIfAnyThrowsWithClassAndCause() {
    MenuErrors errors = new MenuErrors();
    errors.add(String.class, new IllegalStateException("broken"));

    MenuDiscoveryException thrown = assertThrows(MenuDiscoveryException.class, errors::failIfAny);

    assertTrue(thrown.getMessage().contains(String.class.getName()));
    assertTrue(thrown.getMessage().contains("broken"));
  }

  @Test
  void listsEveryFailureSeparatedBySemicolon() {
    MenuErrors errors = new MenuErrors();
    errors.add(Integer.class, new IllegalStateException("first"));
    errors.add(Long.class, new IllegalStateException("second"));

    MenuDiscoveryException thrown = assertThrows(MenuDiscoveryException.class, errors::failIfAny);

    assertTrue(thrown.getMessage().contains("first"));
    assertTrue(thrown.getMessage().contains("second"));
    assertTrue(thrown.getMessage().contains("; "));
  }

  @Test
  void usesTheCauseTypeWhenItCarriesNoMessage() {
    MenuErrors errors = new MenuErrors();
    errors.add(String.class, new NullPointerException());

    MenuDiscoveryException thrown = assertThrows(MenuDiscoveryException.class, errors::failIfAny);

    assertTrue(thrown.getMessage().contains(String.class.getName()));
    assertTrue(thrown.getMessage().contains("NullPointerException"));
  }

  @Test
  void messageNamesTheFailingClassForEachEntry() {
    MenuErrors errors = new MenuErrors();
    errors.add(Integer.class, new IllegalStateException("first"));
    errors.add(Long.class, new IllegalStateException("second"));

    MenuDiscoveryException thrown = assertThrows(MenuDiscoveryException.class, errors::failIfAny);

    assertTrue(thrown.getMessage().contains(Integer.class.getName()));
    assertTrue(thrown.getMessage().contains(Long.class.getName()));
  }
}
