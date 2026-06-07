package dev.haniel.menu.paper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PackageNamesTest {

  @Test
  void acceptsADottedPackage() {
    assertDoesNotThrow(() -> PackageNames.requireValid(new String[] {"com.acme.plugin.menu"}));
  }

  @Test
  void acceptsTwoSegmentPackage() {
    assertDoesNotThrow(() -> PackageNames.requireValid(new String[] {"dev.haniel"}));
  }

  @Test
  void acceptsMultiplePackages() {
    assertDoesNotThrow(
        () -> PackageNames.requireValid(new String[] {"com.acme.menu", "org.example.shop"}));
  }

  @Test
  void rejectsNullArray() {
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> PackageNames.requireValid(null));

    assertEquals("At least one base package is required", error.getMessage());
  }

  @Test
  void rejectsEmptyArray() {
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> PackageNames.requireValid(new String[0]));

    assertEquals("At least one base package is required", error.getMessage());
  }

  @Test
  void rejectsBlankPackage() {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class, () -> PackageNames.requireValid(new String[] {"   "}));

    assertEquals("Base package cannot be blank", error.getMessage());
  }

  @Test
  void rejectsNullElement() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PackageNames.requireValid(new String[] {(String) null}));
  }

  @Test
  void rejectsSingleTokenPackage() {
    assertThrows(
        IllegalArgumentException.class, () -> PackageNames.requireValid(new String[] {"dev"}));
  }

  @Test
  void rejectsTrailingDot() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PackageNames.requireValid(new String[] {"com.acme."}));
  }

  @Test
  void rejectsLeadingDot() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PackageNames.requireValid(new String[] {".com.acme"}));
  }

  @Test
  void rejectsInvalidWhenAnyPackageIsBad() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PackageNames.requireValid(new String[] {"com.acme.menu", "bad"}));
  }
}
