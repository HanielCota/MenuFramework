package dev.haniel.menu.paper;

import java.util.Arrays;
import java.util.regex.Pattern;

final class PackageNames {

  // At least two dotted segments, so a broad single token like "dev" or "org" — which would make
  // ClassGraph crawl half the classpath at boot — is rejected.
  private static final Pattern DOTTED_PACKAGE =
      Pattern.compile("[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)+");

  private PackageNames() {}

  static void requireValid(String[] basePackages) {
    if (basePackages == null) {
      throw new IllegalArgumentException("At least one base package is required");
    }
    if (basePackages.length == 0) {
      throw new IllegalArgumentException("At least one base package is required");
    }
    Arrays.stream(basePackages).forEach(PackageNames::requireValid);
  }

  private static void requireValid(String basePackage) {
    if (basePackage == null || basePackage.isBlank()) {
      throw new IllegalArgumentException("Base package cannot be blank");
    }
    if (!DOTTED_PACKAGE.matcher(basePackage).matches()) {
      throw new IllegalArgumentException(
          "Base package must be a specific dotted package (e.g. com.acme.plugin.menu), was: "
              + basePackage);
    }
  }
}
