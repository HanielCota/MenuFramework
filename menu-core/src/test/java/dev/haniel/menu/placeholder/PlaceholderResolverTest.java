package dev.haniel.menu.placeholder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlaceholderResolverTest {

  @Test
  void noneReturnsTheTextUnchanged() {
    PlaceholderResolver resolver = PlaceholderResolver.none();

    assertEquals("%x% raw", resolver.resolve(new PlayerId(UUID.randomUUID()), "%x% raw"));
  }
}
